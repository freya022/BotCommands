package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
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
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.jvmErasure

@BService
@Dependencies(ComponentRepository::class)
internal class ComponentTimeoutManager(
    private val scopesConfig: BCoroutineScopesConfig,
    private val serviceContainer: ServiceContainer,
    private val componentRepository: ComponentRepository,
    private val groupTimeoutHandlers: GroupTimeoutHandlers,
    private val componentTimeoutHandlers: ComponentTimeoutHandlers
) {
    private val logger = KotlinLogging.logger { }
    private val componentController: ComponentController by serviceContainer.lazy()
    private val timeoutMap = hashMapOf<Int, Job>()

    fun scheduleTimeout(id: Int, expirationTimestamp: Instant) {
        timeoutMap[id] = scopesConfig.componentTimeoutScope.launch {
            delay(expirationTimestamp - Clock.System.now())

            //Remove the ID from the timeout map even if the component doesn't exist (might have been cleaned earlier)
            timeoutMap.remove(id)

            val component = componentRepository.getComponent(id)
                ?: return@launch logger.warn { "Component $id was still timeout scheduled after being deleted" }

            //Will also cancel timeouts of related components
            componentController.deleteComponent(component, isTimeout = true)

            //Throw timeout exceptions
            throwTimeouts(component.componentId)

            when (val componentTimeout = component.timeout) {
                is PersistentTimeout -> {
                    val handlerName = componentTimeout.handlerName ?: return@launch
                    val descriptor = when (component.componentType) {
                        ComponentType.GROUP ->
                            groupTimeoutHandlers[handlerName]
                                ?: return@launch logger.warn { "Could not find group timeout handler: $handlerName" }
                        else ->
                            componentTimeoutHandlers[handlerName]
                                ?: return@launch logger.warn { "Could not find component timeout handler: $handlerName" }
                    }

                    val firstParameter: Any = when (component.componentType) {
                        ComponentType.GROUP -> GroupTimeoutData((component as ComponentGroupData).componentsIds)
                        ComponentType.BUTTON, ComponentType.SELECT_MENU -> ComponentTimeoutData(component.componentId.toString())
                    }

                    val userData = componentTimeout.userData
                    if (userData.size != descriptor.optionSize) {
                        return@launch logger.warn {
                            """
                                Mismatch between button options and ${descriptor.function.shortSignature}
                                Button had ${userData.size} options, function has ${descriptor.optionSize} options
                                Button raw data: $userData
                            """.trimIndent()
                        }
                    }

                    handlePersistentTimeout(descriptor, firstParameter, userData.iterator())
                }
                is EphemeralTimeout -> componentTimeout.handler?.invoke()
            }
        }
    }

    private suspend fun handlePersistentTimeout(
        descriptor: TimeoutDescriptor<*>,
        firstArgument: Any,
        userDataIterator: Iterator<String?>
    ): Boolean {
        with(descriptor) {
            val optionValues = parameters.mapOptions { option ->
                if (tryInsertOption(descriptor, option, this, userDataIterator) == InsertOptionResult.ABORT)
                    return false
            }

            function.callSuspendBy(parameters.mapFinalParameters(firstArgument, optionValues))
        }
        return true
    }

    private suspend fun tryInsertOption(
        descriptor: TimeoutDescriptor<*>,
        option: Option,
        optionMap: MutableMap<Option, Any?>,
        userDataIterator: Iterator<String?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as TimeoutHandlerOption

                userDataIterator.next()?.let { option.resolver.resolve(descriptor, it) }
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                //TODO add CONSTANT option type, make services use it
                serviceContainer.getService(option.type.jvmErasure)
//                option.resolver.resolveSuspend(descriptor, event)
            }
            else -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }

    fun throwTimeouts(componentId: Int) {
        componentController.removeContinuations(componentId).let { continuations ->
            if (continuations.isNotEmpty()) {
                val timeoutException = TimeoutExceptionAccessor.createComponentTimeoutException()
                continuations.forEach {
                    it.cancel(timeoutException)
                }
            }
        }
    }

    fun cancelTimeout(id: Int) {
        logger.trace { "Cancelled timeout for component $id" }
        timeoutMap.remove(id)?.cancel()
    }
}