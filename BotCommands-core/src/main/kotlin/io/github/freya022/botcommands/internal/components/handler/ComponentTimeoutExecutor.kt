package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.components.data.ComponentData
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.utils.throwInternal

@InterfacedService(acceptMultiple = false)
internal interface ComponentTimeoutExecutor {

    suspend fun handleTimeout(component: ComponentData) {
        val timeout = component.timeout
            ?: throwInternal("Component ${component.internalId} was scheduled for timeout but has no timeout")

        when (timeout) {
            is EphemeralTimeout -> handleEphemeralTimeout(component, timeout)
            is PersistentTimeout -> handlePersistentTimeout(component, timeout)
        }
    }

    suspend fun handleEphemeralTimeout(component: ComponentData, timeout: EphemeralTimeout)

    suspend fun handlePersistentTimeout(component: ComponentData, timeout: PersistentTimeout)
}
