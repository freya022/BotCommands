package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.lazy
import io.github.freya022.botcommands.internal.components.handler.ComponentTimeoutExecutor
import io.github.freya022.botcommands.internal.components.repositories.ComponentRepository
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.utils.TimeoutExceptionAccessor
import io.github.freya022.botcommands.internal.utils.launchCatchingDelayed
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.collections.set

private val logger = KotlinLogging.logger { }

@BService
@RequiresComponents
internal class ComponentTimeoutManager(
    private val context: BContext,
    serviceContainer: ServiceContainer,
    private val continuationManager: ComponentContinuationManager,
    private val componentRepository: ComponentRepository,
    private val componentTimeoutExecutor: ComponentTimeoutExecutor,
) {
    private val exceptionHandler = ExceptionHandler(context, logger)
    private val componentController: ComponentController by serviceContainer.lazy()
    private val timeoutMap = hashMapOf<Int, Job>()

    internal fun scheduleTimeout(id: Int, expirationTimestamp: Instant) {
        val delay = expirationTimestamp - Clock.System.now()
        timeoutMap[id] = context.coroutineScopesConfig.componentTimeoutScope.launchCatchingDelayed(
            delay,
            { handleTimeoutException(id, it) },
            { onTimeout(id) }
        )
    }

    private suspend fun onTimeout(id: Int) {
        //Remove the ID from the timeout map even if the component doesn't exist (might have been cleaned earlier)
        timeoutMap.remove(id)

        val component = componentRepository.getComponent(id)
            ?: return logger.warn { "Component $id was still timeout scheduled after being deleted" }

        //Will also cancel timeouts of related components
        componentController.deleteComponent(component, throwTimeouts = true)

        // Run user code
        componentTimeoutExecutor.handleTimeout(component)
    }

    internal fun removeTimeouts(componentId: Int, throwTimeouts: Boolean) {
        logger.trace { "Cancelled timeout for component $componentId" }
        timeoutMap.remove(componentId)?.cancel()

        val continuations = continuationManager.removeContinuations(componentId)
        if (continuations.isEmpty()) return

        // Continuations must be canceled
        if (throwTimeouts) {
            val timeoutException = TimeoutExceptionAccessor.createComponentTimeoutException()
            continuations.forEach { it.cancel(timeoutException) }
        } else {
            val cancellationException = CancellationException("Component was deleted")
            continuations.forEach { it.cancel(cancellationException) }
        }
    }

    internal fun cancelTimeout(componentId: Int) {
        timeoutMap.remove(componentId)?.cancel()
    }

    private fun handleTimeoutException(id: Int, e: Throwable) {
        exceptionHandler.handleException(null, e, "component timeout handler", mapOf("Component ID" to id))
    }
}