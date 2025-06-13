package io.github.freya022.pokedex.bot.commands

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.pokedex.bot.pokedex.Pokedex
import io.github.freya022.pokedex.bot.pokedex.Pokemon
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.build.OptionData

private const val POKEMON_ID_AUTOCOMPLETE_NAME = "SlashPokedexLookup: pokemon"

@Command
class SlashPokedexLookup(
    private val slashPokedex: SlashPokedex,
) : ApplicationCommand() {

    @JDASlashCommand(name = "pokedex-lookup", description = "Lookup pokemon by their id")
    suspend fun onSlashPokedexLookup(event: GuildSlashEvent, @SlashOption(autocomplete = POKEMON_ID_AUTOCOMPLETE_NAME) pokemon: Pokemon) {
        event.reply(slashPokedex.getPokemonView(pokemon, null)).queue()
    }

    @AutocompleteHandler(POKEMON_ID_AUTOCOMPLETE_NAME)
    fun onPokemonAutocomplete(event: CommandAutoCompleteInteractionEvent): List<Choice> {
        val query = event.focusedOption.value
        val pokemons = if (query.isNotBlank())
            Pokedex.pokemons.values.filter { pokemon -> query.startsWith(pokemon.name.english) }
        else
            Pokedex.pokemons.values
        return pokemons.take(OptionData.MAX_CHOICES).map { Choice(it.name.english, it.id.toLong()) }
    }
}