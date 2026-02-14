package io.github.freya022.botcommands.internal.components.handler.local

import io.github.freya022.botcommands.api.components.annotations.RequiresLocalComponents
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.data.ComponentData
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.components.handler.ComponentTimeoutExecutor
import io.github.freya022.botcommands.internal.utils.throwInternal

@BService
@RequiresLocalComponents
internal class LocalComponentTimeoutExecutor : ComponentTimeoutExecutor {

    override suspend fun handleEphemeralTimeout(component: ComponentData, timeout: EphemeralTimeout) {
        timeout.handler.invoke()
    }

    override suspend fun handlePersistentTimeout(component: ComponentData, timeout: PersistentTimeout) {
        throwInternal("Local components should only produce ephemeral timeouts")
    }
}
