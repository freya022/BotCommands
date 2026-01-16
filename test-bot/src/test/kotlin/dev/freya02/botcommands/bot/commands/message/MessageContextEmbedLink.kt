package dev.freya02.botcommands.bot.commands.message

import dev.freya02.botcommands.bot.services.Disabled
import dev.freya02.botcommands.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent

@Command
@Disabled
class MessageContextEmbedLink {
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
