package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.lazy
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.data.ComponentGroupData
import io.github.freya022.botcommands.internal.components.data.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.data.PersistentTimeout
import io.github.freya022.botcommands.internal.components.repositories.ComponentRepository
import io.github.freya022.botcommands.internal.components.timeout.ComponentTimeoutHandlers
import io.github.freya022.botcommands.internal.components.timeout.GroupTimeoutHandlers
import io.github.freya022.botcommands.internal.components.timeout.TimeoutDescriptor
import io.github.freya022.botcommands.internal.components.timeout.TimeoutHandlerOption
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.reflect.full.callSuspendBy

private val logger = KotlinLogging.logger { }

@BService
@RequiresComponents
internal class ComponentTimeoutManager(
    private val context: BContext,
    private val serviceContainer: ServiceContainer,
    private val componentRepository: ComponentRepository,
    private val groupTimeoutHandlers: GroupTimeoutHandlers,
    private val componentTimeoutHandlers: ComponentTimeoutHandlers
) {
    private val exceptionHandler = ExceptionHandler(context, logger)
    private val componentController: ComponentController by serviceContainer.lazy()
    private val timeoutMap = hashMapOf<Int, Job>()

    fun scheduleTimeout(id: Int, expirationTimestamp: Instant) {
        val delay = expirationTimestamp - Clock.System.now()
        timeoutMap[id] = context.coroutineScopesConfig.componentTimeoutScope.launchCatchingDelayed(
            delay,
            { handleTimeoutException(id, it) },
            { onTimeout(id) }
        )
    }

    private fun handleTimeoutException(id: Int, e: Throwable) {
        exceptionHandler.handleException(null, e, "component timeout handler", mapOf("Component ID" to id))
    }

    private suspend fun onTimeout(id: Int) {
        //Remove the ID from the timeout map even if the component doesn't exist (might have been cleaned earlier)
        timeoutMap.remove(id)

        val component = componentRepository.getComponent(id)
            ?: return logger.warn { "Component $id was still timeout scheduled after being deleted" }

        //Will also cancel timeouts of related components
        componentController.deleteComponent(component, throwTimeouts = true)

        when (val componentTimeout = component.timeout) {
            is PersistentTimeout -> {
                val handlerName = componentTimeout.handlerName
                val descriptor = when (component.componentType) {
                    ComponentType.GROUP ->
                        groupTimeoutHandlers[handlerName]
                            ?: return logger.warn { "Missing ${annotationRef<GroupTimeoutHandler>()} named '$handlerName'" }
                    else ->
                        componentTimeoutHandlers[handlerName]
                            ?: return logger.warn { "Missing ${annotationRef<ComponentTimeoutHandler>()} named '$handlerName'" }
                }

                val firstParameter: Any = when (component.componentType) {
                    ComponentType.GROUP -> GroupTimeoutData((component as ComponentGroupData).componentIds)
                    ComponentType.BUTTON, ComponentType.SELECT_MENU -> ComponentTimeoutData(component.internalId)
                }

                val userData = componentTimeout.userData
                if (userData.size != descriptor.optionSize) {
                    return logger.warn {
                        """
                            Mismatch between component options and ${descriptor.function.shortSignature}
                            Component had ${userData.size} options, function has ${descriptor.optionSize} options
                            Component raw data: $userData
                        """.trimIndent()
                    }
                }

                handlePersistentTimeout(descriptor, firstParameter, userData.iterator())
            }
            is EphemeralTimeout -> componentTimeout.handler()
        }
    }

    private suspend fun handlePersistentTimeout(
        descriptor: TimeoutDescriptor<*>,
        firstArgument: Any,
        userDataIterator: Iterator<String?>
    ): Boolean {
        with(descriptor) {
            val optionValues = parameters.mapOptions { option ->
                if (tryInsertOption(option, this, userDataIterator) == InsertOptionResult.ABORT)
                    return false
            }

            function.callSuspendBy(parameters.mapFinalParameters(firstArgument, optionValues))
        }
        return true
    }

    private suspend fun tryInsertOption(
        option: OptionImpl,
        optionMap: MutableMap<OptionImpl, Any?>,
        userDataIterator: Iterator<String?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as TimeoutHandlerOption

                userDataIterator.next()?.let { option.resolver.resolveSuspend(it) }
            }
            OptionType.SERVICE -> (option as ServiceMethodOption).getService()
            OptionType.CUSTOM, OptionType.CONSTANT, OptionType.GENERATED -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }

    fun removeTimeouts(componentId: Int, throwTimeouts: Boolean) {
        logger.trace { "Cancelled timeout for component $componentId" }
        timeoutMap.remove(componentId)?.cancel()

        val continuations = componentController.removeContinuations(componentId)
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
}