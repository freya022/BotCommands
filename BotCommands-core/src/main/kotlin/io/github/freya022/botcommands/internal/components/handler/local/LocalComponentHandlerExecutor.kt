package io.github.freya022.botcommands.internal.components.handler.local

import io.github.freya022.botcommands.api.components.annotations.RequiresLocalComponents
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.data.EphemeralComponentData
import io.github.freya022.botcommands.internal.components.data.PersistentComponentData
import io.github.freya022.botcommands.internal.components.handler.ComponentHandlerExecutor
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
@RequiresLocalComponents
internal class LocalComponentHandlerExecutor : ComponentHandlerExecutor {

    override suspend fun runEphemeralHandler(
        component: EphemeralComponentData,
        handler: EphemeralHandler<GenericComponentInteractionCreateEvent>,
        event: GenericComponentInteractionCreateEvent
    ): Boolean {
        handler.handler.invoke(event)
        return true
    }

    override suspend fun runPersistentHandler(
        component: PersistentComponentData,
        handler: PersistentHandler,
        event: GenericComponentInteractionCreateEvent
    ): Boolean {
        throwInternal("Local components should only produce ephemeral handlers")
    }
}
