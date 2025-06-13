package io.github.freya022.pokedex.bot.pokedex

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.PreFirstGatewayConnectEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji
import kotlin.io.path.nameWithoutExtension

private val logger = KotlinLogging.logger { }

@BService
class PokemonEmojisService {

    lateinit var emojis: Map<Int, List<ApplicationEmoji>>
        private set

    @BEventListener(mode = BEventListener.RunMode.BLOCKING)
    suspend fun onInjectedJDA(event: PreFirstGatewayConnectEvent) {
        val appEmojis: MutableList<ApplicationEmoji> = event.jda.retrieveApplicationEmojis().await()

        emojis = Pokedex.pokemons.values.associate { pokemon ->
            pokemon.id to pokemon.emojiPaths.map { emojiPath ->
                val emojiName = emojiPath.nameWithoutExtension
                val existingEmoji = appEmojis.find { it.name == emojiName }
                if (existingEmoji != null) return@map existingEmoji

                logger.info { "Inserting emoji '$emojiName'" }
                event.jda.createApplicationEmoji(emojiName, Icon.from(emojiPath.toFile())).await()
            }
        }
    }
}

fun Pokemon.getEmojis(pokemonEmojisService: PokemonEmojisService): List<ApplicationEmoji> {
    return pokemonEmojisService.emojis[id] ?: error("No emojis for Pokemon ID $id")
}