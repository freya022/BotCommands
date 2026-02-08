package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BStatusChangeEvent
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.RequiresDefaultInjection
import io.github.freya022.botcommands.api.core.service.getService

@Lazy
@BService
@ConditionalService(BCShutdownHook.ActivationCondition::class)
@RequiresDefaultInjection
internal class BCShutdownHook internal constructor(
    context: BContext,
) {

    private val hook = Thread { context.shutdownNow() }

    @BEventListener
    internal fun registerShutdownHook(event: PostLoadEvent) {
        Runtime.getRuntime().addShutdownHook(hook)
    }

    @BEventListener
    internal fun onShuttingDown(event: BStatusChangeEvent) {
        if (event.newStatus == BContext.Status.SHUTTING_DOWN) {
            try {
                Runtime.getRuntime().removeShutdownHook(hook)
            } catch (_: IllegalStateException) {
                // Already shutting down
            }
        }
    }

    internal object ActivationCondition : ConditionalServiceChecker {

        override fun checkServiceAvailability(
            serviceContainer: ServiceContainer,
            checkedClass: Class<*>
        ): String? {
            if (!serviceContainer.getService<BConfig>().enableShutdownHook) {
                return "Default shutdown hook is disabled"
            }

            return null
        }
    }
}
