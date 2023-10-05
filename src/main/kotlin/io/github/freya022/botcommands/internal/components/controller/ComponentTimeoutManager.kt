package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.components.data.ComponentTimeout
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
import io.github.freya022.botcommands.internal.components.repositories.ComponentTimeoutHandlers
import io.github.freya022.botcommands.internal.components.repositories.GroupTimeoutHandlers
import io.github.freya022.botcommands.internal.core.reflection.MemberFunction
import io.github.freya022.botcommands.internal.utils.TimeoutExceptionAccessor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import mu.KotlinLogging
import kotlin.reflect.KParameter
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

    fun scheduleTimeout(id: Int, timeout: ComponentTimeout) {
        timeoutMap[id] = scopesConfig.componentTimeoutScope.launch {
            delay(timeout.expirationTimestamp - Clock.System.now())

            //Remove the ID from the timeout map even if the component doesn't exist (might have been cleaned earlier)
            timeoutMap.remove(id)

            val component = componentRepository.getComponent(id)
            if (component == null) {
                logger.warn("Component $id was still timeout scheduled after being deleted")
                return@launch
            }

            //Will also cancel timeouts of related components
            componentController.deleteComponent(component, isTimeout = true)

            //Throw timeout exceptions
            throwTimeouts(component.componentId)

            when (val componentTimeout = component.timeout) {
                is PersistentTimeout -> {
                    val handlerName = componentTimeout.handlerName ?: return@launch
                    val handler = when (component.componentType) {
                        ComponentType.GROUP -> groupTimeoutHandlers[handlerName] ?: let {
                            logger.warn("Could not find group timeout handler: $handlerName")
                            return@launch
                        }
                        else -> componentTimeoutHandlers[handlerName] ?: let {
                            logger.warn("Could not find component timeout handler: $handlerName")
                            return@launch
                        }
                    }

                    val firstParameter: Any = when (component.componentType) {
                        ComponentType.GROUP -> GroupTimeoutData((component as ComponentGroupData).componentsIds)
                        ComponentType.BUTTON, ComponentType.SELECT_MENU -> ComponentTimeoutData(component.componentId.toString())
                    }

                    callTimeoutHandler(handler, firstParameter)
                }
                is EphemeralTimeout -> componentTimeout.handler?.invoke()
            }
        }
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

    private suspend fun callTimeoutHandler(handler: MemberFunction<*>, firstArgument: Any): Any? {
        val args = buildMap<KParameter, Any?>(handler.parametersSize) {
            this[handler.instanceParameter] = handler.instance
            this[handler.firstParameter] = firstArgument

            handler.resolvableParameters.forEach { kParameter ->
                this[kParameter] = serviceContainer.getService(kParameter.type.jvmErasure)
            }
        }

        return handler.kFunction.callSuspendBy(args)
    }
}