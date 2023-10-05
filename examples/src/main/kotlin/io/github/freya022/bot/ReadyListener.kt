package io.github.freya022.bot

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ReadyEvent

private val logger = KotlinLogging.logger { }

// You can optionally have a name to differentiate between multiple instance of your services
@BService(name = "myReadyListener")
class ReadyListener {
    @BEventListener(priority = 1)
    fun onReadyFirst(event: ReadyEvent) {
        logger.info("First handling of ReadyEvent")

        val jda = event.jda

        //Print some information about the bot
        logger.info("Bot connected as ${jda.selfUser.name}")
        logger.info("The bot is present on these guilds :")
        for (guild in jda.guildCache) {
            logger.info("\t- ${guild.name} (${guild.id})")
        }
    }

    @BEventListener(priority = 0) // Executes after the above listener
    fun onReadyLast(event: ReadyEvent) {
        logger.info("Last handling of ReadyEvent")
    }
}