package com.freya02.botcommands.test.commands_kt

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.MarkdownSanitizer

@CommandMarker
class MessageContextRaw : ApplicationCommand() {
    @CommandMarker
    fun onMessageContextRaw(event: GlobalMessageEvent, message: Message, rawContent: String) {
        event.reply_("Raw for message ID ${message.id}: $rawContent", ephemeral = true).queue()
    }

    @Declaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.messageCommand("Raw content") {
            option("message")

            generatedOption("rawContent") {
                it as MessageContextInteractionEvent

                MarkdownSanitizer.escape(it.target.contentRaw)
            }

            function = ::onMessageContextRaw
        }
    }
}