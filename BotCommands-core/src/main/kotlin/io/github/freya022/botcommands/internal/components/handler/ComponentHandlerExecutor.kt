package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.components.data.ActionComponentData
import io.github.freya022.botcommands.internal.components.data.EphemeralComponentData
import io.github.freya022.botcommands.internal.components.data.PersistentComponentData
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@InterfacedService(acceptMultiple = false)
internal interface ComponentHandlerExecutor {
    suspend fun runHandler(component: ActionComponentData, event: GenericComponentInteractionCreateEvent): Boolean {
        val handler = component.handler ?: return true

        when (component) {
            is PersistentComponentData -> {
                return runPersistentHandler(component, handler as PersistentHandler, event)
            }

            is EphemeralComponentData -> {
                @Suppress("UNCHECKED_CAST")
                return runEphemeralHandler(
                    component,
                    handler as EphemeralHandler<GenericComponentInteractionCreateEvent>,
                    event
                )
            }
        }
    }

    suspend fun runEphemeralHandler(
        component: EphemeralComponentData,
        handler: EphemeralHandler<GenericComponentInteractionCreateEvent>,
        event: GenericComponentInteractionCreateEvent,
    ): Boolean

    suspend fun runPersistentHandler(
        component: PersistentComponentData,
        handler: PersistentHandler,
        event: GenericComponentInteractionCreateEvent,
    ): Boolean
}
