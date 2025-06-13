package io.github.freya022.pokedex.bot.pokedex

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.PreFirstGatewayConnectEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.pokedex.bot.pokedex.data.PokemonDataFetcher
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

@BService
class PokemonEmojisService(
    private val pokemonDataFetcher: PokemonDataFetcher,
    private val pokedex: Pokedex,
) {

    lateinit var emojis: Map<Int, List<ApplicationEmoji>>
        private set

    @BEventListener(mode = BEventListener.RunMode.BLOCKING)
    suspend fun onInjectedJDA(event: PreFirstGatewayConnectEvent) {
        try {
            val appEmojis: Map<String, ApplicationEmoji> = event.jda.retrieveApplicationEmojis().await().associateBy { it.name }

            emojis = pokedex.pokemons.keys.associateWith { pokemonId ->
                pokemonDataFetcher.getPokemonEmojis(pokemonId).map { emojiAsset ->
                    val emojiName = emojiAsset.name.substringBeforeLast('.')
                    appEmojis[emojiName] ?: run {
                        logger.info { "Inserting emoji '$emojiName'" }
                        event.jda.createApplicationEmoji(emojiName, emojiAsset.toIcon()).await()
                    }
                }
            }
        } catch (e: Exception) {
            logger.catching(e)
            exitProcess(2)
        }
    }
}

fun Pokemon.getEmojis(pokemonEmojisService: PokemonEmojisService): List<ApplicationEmoji> {
    return pokemonEmojisService.emojis[id] ?: error("No emojis for Pokemon ID $id")
}