package com.freya02.botcommands.test.commands_kt.message

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.GuildApplicationCommandManager
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.MarkdownSanitizer

@CommandMarker
class MessageContextRaw {
    @Declaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.messageCommand("Raw content", CommandScope.GUILD) {
            option("message")

            generatedOption("rawContent") {
                it as MessageContextInteractionEvent

                MarkdownSanitizer.escape(it.target.contentRaw)
            }

            function = MessageContextRawAnnotated::onMessageContextRaw
        }
    }
}