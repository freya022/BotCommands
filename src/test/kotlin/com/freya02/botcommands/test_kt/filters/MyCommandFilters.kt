package com.freya02.botcommands.test_kt.filters

import com.freya02.botcommands.api.commands.application.ApplicationCommandFilter
import com.freya02.botcommands.api.commands.application.ApplicationFilteringData
import com.freya02.botcommands.api.commands.prefixed.TextCommandFilter
import com.freya02.botcommands.api.commands.prefixed.TextFilteringData
import com.freya02.botcommands.api.core.service.annotations.BService
import dev.minn.jda.ktx.messages.reply_

@BService
class MyCommandFilters : TextCommandFilter, ApplicationCommandFilter {
    override fun isAccepted(data: TextFilteringData): Boolean {
        return data.event.channel.idLong == 722891685755093076
    }

    override fun isAccepted(data: ApplicationFilteringData): Boolean {
        if (data.event.channel?.idLong != 722891685755093076) {
            data.event.reply_("Commands are not allowed in this channel", ephemeral = true).queue()
            return false
        }
        return true
    }
}