package io.github.freya022.botcommands.test_kt.commands.message

import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent

@Command
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