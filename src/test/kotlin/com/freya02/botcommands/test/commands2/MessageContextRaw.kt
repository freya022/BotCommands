package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.MarkdownSanitizer

@CommandMarker
class MessageContextRaw : ApplicationCommand() {
    @CommandMarker
    fun onMessageContextRaw(event: GlobalMessageEvent, message: Message) {
        event.reply_("Raw: ${MarkdownSanitizer.escape(message.contentRaw)}", ephemeral = true).queue()
    }

    @Declaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.messageCommand("Raw content") {
            option("message")

            function = ::onMessageContextRaw
        }
    }
}