package com.freya02.botcommands.test_kt.filters

import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.core.service.annotations.BService
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
class MyComponentFilter : ComponentInteractionFilter {
    override fun isAccepted(event: GenericComponentInteractionCreateEvent): Boolean {
        if (event.channel.idLong != 722891685755093076) {
            event.reply_("Components are not allowed in this channel", ephemeral = true).queue()
            return false
        }
        return true
    }
}