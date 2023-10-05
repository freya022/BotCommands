package io.github.freya022.botcommands.test_kt.filters

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.prefixed.TextCommandFilter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
class MyCommandFilters : TextCommandFilter, ApplicationCommandFilter {
    override suspend fun isAcceptedSuspend(event: MessageReceivedEvent, commandVariation: TextCommandVariation, args: String): Boolean {
        return event.channel.idLong == 722891685755093076
    }

    override suspend fun isAcceptedSuspend(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean {
        if (event.channel?.idLong != 722891685755093076) {
            event.reply_("Commands are not allowed in this channel", ephemeral = true).queue()
            return false
        }
        return true
    }
}