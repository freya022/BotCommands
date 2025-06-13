package io.github.freya022.pokedex.bot.commands

import dev.freya02.jda.emojis.unicode.Emojis
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.MessageCreate
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.utils.edit
import io.github.freya022.botcommands.api.core.utils.send
import io.github.freya022.botcommands.api.core.utils.toEditData
import io.github.freya022.pokedex.bot.pokedex.Pokedex
import io.github.freya022.pokedex.bot.pokedex.Pokemon
import io.github.freya022.pokedex.bot.pokedex.PokemonEmojisService
import io.github.freya022.pokedex.bot.pokedex.data.PokemonDataFetcher
import io.github.freya022.pokedex.bot.pokedex.data.getRandomImages
import io.github.freya022.pokedex.bot.pokedex.data.getThumbnail
import io.github.freya022.pokedex.bot.pokedex.getEmojis
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.utils.messages.MessageCreateData

private const val pokemonDataUrl = "https://github.com/Purukitto/pokemon-data.json"
private const val pokemonImagesUrl = "https://www.kaggle.com/datasets/vishalsubbiah/pokemon-images-and-types"

@Command
class SlashPokedex(
    private val pokemonDataFetcher: PokemonDataFetcher,
    private val pokemonEmojisService: PokemonEmojisService,
    private val pokedex: Pokedex,
    private val buttons: Buttons,
) : ApplicationCommand() {

    @JDASlashCommand(name = "pokedex", description = "Shows the pokédex for the OG 151 pokémons")
    suspend fun onSlashPokedex(event: GuildSlashEvent) {
        getPokedexPage(pokedex.getPage(0), 0)
            .send(event)
            .queue()
    }

    private suspend fun getPokedexPage(pokemons: List<Pokemon>, currentPage: Int): MessageCreateData = MessageCreate(useComponentsV2 = true) {
        components += Container {
            +MediaGallery {
                +pokemonDataFetcher.getPokedexHeader().toMediaGalleryItem()
            }

            pokemons.forEach { pokemon ->
                +pokemonSection(pokemon) {
                    bindWith(::onViewPokemonClicked, pokemon.id, currentPage)
                }
            }

            +ActionRow {
                if (currentPage > 0) {
                    +buttons.primary("Prev").persistent {
                        bindWith(::onChangePageClicked, currentPage - 1)
                    }
                } else {
                    +buttons.primary("Prev").toLabelButton()
                }

                +buttons.secondary("Page (${currentPage + 1} / ${pokedex.maxPage + 1})").toLabelButton()

                if (currentPage < pokedex.maxPage) {
                    +buttons.primary("Next").persistent {
                        bindWith(::onChangePageClicked, currentPage + 1)
                    }
                } else {
                    +buttons.primary("Next").toLabelButton()
                }
            }
        }
    }

    @JDAButtonListener
    suspend fun onChangePageClicked(event: ButtonEvent, @ComponentData page: Int) {
        getPokedexPage(pokedex.getPage(page), page)
            .toEditData()
            .edit(event)
            .await()
    }

    @JDAButtonListener
    suspend fun onViewPokemonClicked(event: ButtonEvent, @ComponentData pokemonId: Int, @ComponentData pokedexPage: Int?) {
        getPokemonView(pokedex.getById(pokemonId), pokedexPage)
            .toEditData()
            .edit(event)
            .await()
    }

    suspend fun getPokemonView(pokemon: Pokemon, pokedexPage: Int?): MessageCreateData = MessageCreate(useComponentsV2 = true) {
        components += Container {
            +Section(
                accessory = pokemon.getThumbnail(pokemonDataFetcher).toThumbnail()
            ) {
                +TextDisplay("**${pokemon.name.english}**")
                +TextDisplay(pokemon.description)
            }

            +Separator(isDivider = true, spacing = Separator.Spacing.SMALL)

            pokemon.evolution.prev?.let { prevEvolution ->
                +TextDisplay("**Previous Evolution**")
                val prevPokemon = pokedex.getById(prevEvolution.id)
                +pokemonSection(prevPokemon) {
                    bindWith(::onViewPokemonClicked, prevPokemon.id, pokedexPage)
                }
            }

            if (pokemon.evolution.next != null) {
                +TextDisplay("**Next Evolution**")
                pokemon.evolution.next.forEach { nextEvolution ->
                    val nextPokemon = pokedex.getById(nextEvolution.id)
                    +pokemonSection(nextPokemon, "-# Criteria: ${nextEvolution.criteria.joinToString()}") {
                        bindWith(::onViewPokemonClicked, nextPokemon.id, pokedexPage)
                    }
                }
            }

            +MediaGallery {
                pokemon.getRandomImages(pokemonDataFetcher, 4).forEach { randomImageAsset ->
                    +randomImageAsset.toMediaGalleryItem()
                }
            }

            +Section(
               accessory = buttons.secondary("More images", Emojis.ARROWS_COUNTERCLOCKWISE).persistent {
                   // Just recreate the same view, random will do its job
                   // this could be optimized by having a different function take the message and replace the exact media gallery
                   // but here it doesn't matter since the other stuff uses URLs
                   bindWith(::onViewPokemonClicked, pokemon.id, pokedexPage)
               }
            ) {
                +TextDisplay("-# Data for this pokemon comes from [pokemon.json]($pokemonDataUrl) and images from [dataset]($pokemonImagesUrl)")
            }
        }

        if (pokedexPage != null) {
            components += ActionRow {
                +buttons.secondary("Return to Pokédex").persistent {
                    bindWith(::onChangePageClicked, pokedexPage)
                }
            }
        }
    }

    private suspend fun pokemonSection(pokemon: Pokemon, additionalText: String? = null, viewButtonConfigurer: PersistentButtonBuilder.() -> Unit): Section {
        val emojis = pokemon.getEmojis(pokemonEmojisService)

        return Section(accessory = buttons.secondary("View").persistent(viewButtonConfigurer)) {
            +TextDisplay(
                buildString {
                    append(
                        """
                            ${emojis[0].formatted}${emojis[1].formatted} **${pokemon.name.english}**
                            ${emojis[2].formatted}${emojis[3].formatted} ${pokemon.species} - ${pokemon.type.joinToString()}
                        """.trimIndent()
                    )

                    additionalText?.let { appendLine().append(it) }
                }
            )
        }
    }
}