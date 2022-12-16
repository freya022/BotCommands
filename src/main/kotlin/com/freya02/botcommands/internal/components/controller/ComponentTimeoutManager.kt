package com.freya02.botcommands.internal.components.controller

import com.freya02.botcommands.api.components.data.ComponentTimeout
import com.freya02.botcommands.api.components.data.ComponentTimeoutData
import com.freya02.botcommands.api.components.data.GroupTimeoutData
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.data.ComponentGroupData
import com.freya02.botcommands.internal.components.data.EphemeralTimeout
import com.freya02.botcommands.internal.components.data.PersistentTimeout
import com.freya02.botcommands.internal.components.repositories.ComponentRepository
import com.freya02.botcommands.internal.components.repositories.ComponentTimeoutHandlers
import com.freya02.botcommands.internal.components.repositories.GroupTimeoutHandlers
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import mu.KotlinLogging
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@ConditionalService(dependencies = [ComponentRepository::class])
internal class ComponentTimeoutManager(
    private val scopesConfig: BCoroutineScopesConfig,
    private val serviceContainer: ServiceContainer,
    private val componentRepository: ComponentRepository,
    private val groupTimeoutHandlers: GroupTimeoutHandlers,
    private val componentTimeoutHandlers: ComponentTimeoutHandlers
) {
    private val logger = KotlinLogging.logger { }
    private val componentController: ComponentController by lazy { serviceContainer.getService() }
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
                val timeoutException = Utils.createComponentTimeoutException()
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

    private suspend fun callTimeoutHandler(handler: KFunction<*>, firstParameter: Any): Any? {
        val args = hashMapOf<KParameter, Any?>()
        args[handler.instanceParameter!!] = serviceContainer.getFunctionService(handler)
        args[handler.valueParameters.first()] = firstParameter

        handler.valueParameters.drop(1).forEach { kParameter ->
            args[kParameter] = serviceContainer.getService(kParameter.type.jvmErasure)
        }

        return handler.callSuspendBy(args)
    }
}