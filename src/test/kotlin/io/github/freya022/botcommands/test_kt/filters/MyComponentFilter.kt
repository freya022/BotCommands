package io.github.freya022.botcommands.test_kt.filters

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
class MyComponentFilter(private val config: BConfig) : ComponentInteractionFilter {
    override suspend fun isAcceptedSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): Boolean {
        if (event.channel.idLong == 932902082724380744 && event.user.idLong !in config.ownerIds) {
            event.reply_("Only owners are allowed to use components", ephemeral = true).queue()
            return false
        }
        return true
    }
}