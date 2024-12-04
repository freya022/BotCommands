package io.github.freya022.botcommands.internal.core.hooks

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BGenericEvent
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.internal.core.*
import io.github.freya022.botcommands.internal.core.exceptions.InternalException
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.core.service.getParameters
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

private typealias EventMap = MutableMap<KClass<*>, EventListenerList>

private val logger = KotlinLogging.logger { }

internal class EventListenerRegistry internal constructor(
    private val config: BConfig,
    private val serviceContainer: ServiceContainer,
    originalCoroutineEventManager: CoroutineEventManager,
    private val eventTreeService: EventTreeService,
    private val jdaService: JDAService,
    functionAnnotationsMap: FunctionAnnotationsMap,
) {

    private val eventTimeout: Duration = originalCoroutineEventManager.timeout

    private val map: EventMap = ConcurrentHashMap()
    private val listeners: MutableMap<Class<*>, EventMap> = ConcurrentHashMap()

    init {
        functionAnnotationsMap
            .get<BEventListener>()
            .addAsEventListeners()
    }

    internal operator fun get(eventType: KClass<*>): EventListenerList? {
        return map[eventType]
    }

    internal fun addEventListener(listener: Any) {
        listener::class
            .functions
            .withFilter(FunctionFilter.annotation<BEventListener>())
            .toClassPathFunctions(listener)
            .addAsEventListeners()
    }

    internal fun removeEventListener(listener: Any) {
        listeners.remove(listener::class.java)?.let { instanceMap ->
            instanceMap.forEach { (kClass, functions) ->
                val functionMap = map[kClass]
                    ?: throwInternal("Listener was registered without having its functions added to the listener map")
                if (!functionMap.removeAll(functions)) {
                    logger.error(InternalException("Unable to remove listener functions from registered functions")) { "An exception occurred while removing event listener $listener" }
                }
            }
        }
    }

    private fun Collection<ClassPathFunction>.addAsEventListeners() = this
        .requiredFilter(FunctionFilter.nonStatic())
        .requiredFilter(FunctionFilter.firstArg(GenericEvent::class, BGenericEvent::class))
        .forEach { classPathFunc ->
            val function = classPathFunc.function
            val annotation = function.findAnnotationRecursive<BEventListener>()
                ?: throwInternal(function, "Function was asserted to have BEventListener but it was not found")

            val parameters = function.nonInstanceParameters

            val eventErasure = parameters.first().type.jvmErasure
            if (!annotation.ignoreIntents && eventErasure.isSubclassOf<Event>()) {
                @Suppress("UNCHECKED_CAST")
                val requiredIntents = GatewayIntent.fromEvents(eventErasure.java as Class<out Event>)
                val missingIntents = requiredIntents - jdaService.intents - config.ignoredIntents
                if (missingIntents.isNotEmpty()) {
                    return@forEach logger.debug { "Skipping event listener ${function.shortSignature} as it is missing intents: $missingIntents" }
                }

                // Cannot check for RawGatewayEvent as JDA is not present yet and there is no config for it
            }

            val eventParametersErasures = parameters.drop(1).map { it.type.jvmErasure }
                // The main risk was with injected services, as they may not be available at that point,
                // but they are pretty much limited to objects manually added by the framework, before the service loading occurs
                .onEach {
                    serviceContainer.canCreateService(it)?.let { serviceError ->
                        throwArgument(
                            classPathFunc.function,
                            "Unable to register event listener due to an unavailable service: ${serviceError.toSimpleString()}"
                        )
                    }
                }
            @Suppress("DEPRECATION")
            val eventHandlerFunction = EventHandlerFunction(classPathFunction = classPathFunc,
                runMode = if (annotation.async) RunMode.ASYNC else annotation.mode,
                timeout = getTimeout(annotation),
                priority = annotation.priority,
                parametersBlock = {
                    //Getting services is delayed until execution, as to ensure late services can be used in listeners
                    serviceContainer.getParameters(eventParametersErasures).toTypedArray()
                })

            classPathFunc.function.declaringClass.java.let { clazz ->
                val instanceMap = listeners.computeIfAbsent(clazz) { hashMapOf() }

                (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                    instanceMap.computeIfAbsent(it) { EventListenerList() }.add(eventHandlerFunction)
                }
            }

            (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                map.computeIfAbsent(it) { EventListenerList() }.add(eventHandlerFunction)
            }
        }

    private fun getTimeout(annotation: BEventListener): Duration {
        if (annotation.timeout < 0) return Duration.INFINITE

        return annotation.timeout.toDuration(annotation.timeoutUnit.toDurationUnit()).let {
            when {
                it.isPositive() && it.isFinite() -> it
                else -> eventTimeout // Inherit from the (possibly user-provided) CoroutineEventManager
            }
        }
    }
}