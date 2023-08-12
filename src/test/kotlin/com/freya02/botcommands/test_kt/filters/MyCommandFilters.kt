package com.freya02.botcommands.test_kt.filters

import com.freya02.botcommands.api.commands.application.ApplicationCommandFilter
import com.freya02.botcommands.api.commands.prefixed.TextCommandFilter
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
class MyCommandFilters : TextCommandFilter, ApplicationCommandFilter {
    override fun isAccepted(event: MessageReceivedEvent, commandInfo: TextCommandInfo, args: String): Boolean {
        return event.channel.idLong == 722891685755093076
    }

    override fun isAccepted(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Boolean {
        if (event.channel?.idLong != 722891685755093076) {
            event.reply_("Commands are not allowed in this channel", ephemeral = true).queue()
            return false
        }
        return true
    }
}