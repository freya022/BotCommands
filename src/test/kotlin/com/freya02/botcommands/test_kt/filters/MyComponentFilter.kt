package com.freya02.botcommands.test_kt.filters

import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.service.annotations.BService
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
class MyComponentFilter(private val config: BConfig) : ComponentInteractionFilter {
    override suspend fun isAcceptedSuspend(event: GenericComponentInteractionCreateEvent): Boolean {
        if (event.user.idLong !in config.ownerIds) {
            event.reply_("Only owners are allowed to use components", ephemeral = true).queue()
            return false
        }
        return true
    }
}