package io.github.freya022.bot

import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.BService
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ReadyEvent

private val logger = KotlinLogging.logger { }

@BService(name = "myReadyListener")
class ReadyListener {
    @BEventListener
    fun onReady(event: ReadyEvent) {
        val jda = event.jda

        //Print some information about the bot
        logger.info("Bot connected as ${jda.selfUser.name}")
        logger.info("The bot is present on these guilds :")
        for (guild in jda.guildCache) {
            logger.info("\t- ${guild.name} (${guild.id})")
        }
    }
}