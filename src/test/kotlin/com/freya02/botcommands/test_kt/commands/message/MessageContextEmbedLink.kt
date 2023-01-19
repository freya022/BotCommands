package com.freya02.botcommands.test_kt.commands.message

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import dev.minn.jda.ktx.messages.Embed

@CommandMarker
class MessageContextEmbedLink : ApplicationCommand() {
    @JDAMessageCommand(name = "Embed link to message")
    fun onMessageContextEmbedLinkToMessage(event: GuildMessageEvent) {
        Embed {
            field {
                name = "Link"
                value = "[here](${event.target.jumpUrl})"
            }
        }.also { event.replyEmbeds(it).setEphemeral(true).queue() }
    }
}