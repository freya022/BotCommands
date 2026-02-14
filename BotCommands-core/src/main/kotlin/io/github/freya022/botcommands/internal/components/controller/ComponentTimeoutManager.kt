package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.exceptions.ComponentTimeoutException
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.LazyService
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.handler.ComponentTimeoutExecutor
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

private val logger = KotlinLogging.logger { }

@BService
@RequiresComponents
internal class ComponentTimeoutManager(
    private val context: BContext,
    private val continuationManager: ComponentContinuationManager,
    componentController: LazyService<ComponentController>,
    private val componentTimeoutExecutor: ComponentTimeoutExecutor,
) {
    private val exceptionHandler = ExceptionHandler(context, logger)
    private val componentController: ComponentController by componentController
    private val timeoutMap = hashMapOf<Int, Job>()

    internal fun scheduleTimeout(id: Int, expirationTimestamp: Instant) {
        val delay = expirationTimestamp - Clock.System.now()
        timeoutMap[id] = context.coroutineScopesConfig.componentTimeoutScope.launch {
            delay(delay)
            onTimeout(id)
        }
    }

    private suspend fun onTimeout(id: Int) {
        try {
            //Remove the ID from the timeout map even if the component doesn't exist (might have been cleaned earlier)
            timeoutMap.remove(id)

            val component = componentController.getComponent(id)
                ?: return logger.warn { "Component $id was still timeout scheduled after being deleted" }

            //Will also cancel timeouts of related components
            componentController.deleteComponent(component, throwTimeouts = true)

            // Run user code, if set
            component.timeout?.let { timeout ->
                componentTimeoutExecutor.handleTimeout(component, timeout)
            }
        } catch (e: Exception) {
            handleTimeoutException(id, e)
        }
    }

    internal fun removeTimeouts(componentId: Int, throwTimeouts: Boolean) {
        logger.trace { "Cancelled timeout for component $componentId" }
        timeoutMap.remove(componentId)?.cancel()

        val continuations = continuationManager.removeContinuations(componentId)
        if (continuations.isEmpty()) return

        // Continuations must be canceled
        val exception = when {
            throwTimeouts -> ComponentTimeoutException("Timed out waiting for component")
            else -> CancellationException("Component was deleted")
        }
        continuations.forEach { it.cancel(exception) }
    }

    internal fun cancelTimeout(componentId: Int) {
        timeoutMap.remove(componentId)?.cancel()
    }

    private fun handleTimeoutException(id: Int, e: Throwable) {
        if (e is CancellationException)
            return logger.trace(e) { "Component timeout handler of ID $id was cancelled" }

        exceptionHandler.handleException(null, e, "component timeout handler", mapOf("Component ID" to id))
    }
}
