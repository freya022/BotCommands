package io.github.freya022.bot

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.events.session.ReadyEvent
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger { }

// You can optionally have a name to differentiate between multiple instance of your services
@BService(name = "myReadyListener")
class ReadyListener {
    @BEventListener(priority = 1)
    fun onReadyFirst(event: ReadyEvent) {
        logger.info { "First handling of ReadyEvent" }

        val jda = event.jda

        //Print some information about the bot
        logger.info { "Bot connected as ${jda.selfUser.name}" }
        logger.info { "The bot is present on these guilds :" }
        for (guild in jda.guildCache) {
            logger.info { "\t- ${guild.name} (${guild.id})" }
        }
    }

    // Executes after the above listener, but doesn't prevent the listener below from running
    @BEventListener(priority = 0, async = true)
    suspend fun onReadyAsync(event: ReadyEvent) {
        logger.info { "(Before) Async handling of ReadyEvent" }
        delay(200.milliseconds)
        logger.info { "(After) Async handling of ReadyEvent" }
    }

    // Executes after the above listener
    @BEventListener(priority = -1)
    fun onReadyLast(event: ReadyEvent) {
        logger.info { "Last handling of ReadyEvent" }
    }
}