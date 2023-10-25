package io.github.freya022.botcommands.test.filters

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
class MyComponentFilter(private val config: BConfig) : ComponentInteractionFilter {
    override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): Any? {
        if (event.channel.idLong == 932902082724380744 && event.user.idLong !in config.ownerIds) {
            return "Only owners are allowed to use components in <#932902082724380744>"
        }

        return null
    }
}

@BService
class MyComponentRejectionHandler : ComponentInteractionRejectionHandler {
    override suspend fun handleSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?,
        userData: Any
    ) {
        event.reply_(userData as String, ephemeral = true).await()
    }
}