package io.github.freya022.botcommands.internal.core.hooks

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BGenericEvent
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.core.service.canCreateWrappedService
import io.github.freya022.botcommands.internal.core.service.tryGetWrappedService
import io.github.freya022.botcommands.internal.core.toClassPathFunctions
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

private val logger = KotlinLogging.logger { }

@BService
internal class EventListenerRegistry internal constructor(
    private val config: BConfig,
    private val serviceContainer: ServiceContainer,
    private val jdaService: JDAService,
    functionAnnotationsMap: FunctionAnnotationsMap,
) {

    private val defaultTimeout: Duration = config.eventManagerConfig.defaultTimeout ?: Duration.INFINITE

    /** Listener instance -> handlers */
    private val listenerFunctionMap: MutableMap<Any, List<EventHandlerFunction>> = ConcurrentHashMap()
    /** Maps event types to the listener subtypes they can fire to */
    private val resolvedListeners: MutableMap<Class<*>, EventListenerList> = ConcurrentHashMap()

    init {
        functionAnnotationsMap
            .get<BEventListener>()
            .addAsEventListeners()
    }

    internal operator fun get(eventType: Class<*>): EventListenerList {
        return resolvedListeners.computeIfAbsent(eventType) { eventType ->
            // Create the list of handlers that are to be fired by the provided event type
            val list = EventListenerList()
            for (handlerFunction in listenerFunctionMap.values.flatten()) {
                val eventErasure = handlerFunction.eventType
                if (eventErasure.isAssignableFrom(eventType)) {
                    list.add(handlerFunction)
                }
            }

            list
        }
    }

    internal fun addEventListener(listener: Any) {
        listener::class
            .functions
            .withFilter(FunctionFilter.annotation<BEventListener>())
            .toClassPathFunctions(listener)
            .addAsEventListeners()

        // Clear the "event type -> handlers" associations, since we added a new listener, the handlers need to be recomputed
        resolvedListeners.clear()
    }

    internal fun removeEventListener(listener: Any) {
        val handlerFunctions = listenerFunctionMap.remove(listener) ?: return

        // Remove handlers of the provided listener from the resolved listeners
        for (listenerList in resolvedListeners.values) {
            listenerList.removeAll(handlerFunctions)
        }
    }

    private fun Collection<ClassPathFunction>.addAsEventListeners() = this
        .requiredFilter(FunctionFilter.nonStatic())
        .requiredFilter(FunctionFilter.firstArgNot(Any::class))
        .requiredFilter(FunctionFilter.noOptional())
        .forEach { classPathFunc ->
            val function = classPathFunc.function
            val annotation = function.findAnnotationRecursive<BEventListener>()
                ?: throwInternal(function, "Function was asserted to have BEventListener but it was not found")

            val parameters = function.nonInstanceParameters

            val eventErasure = parameters.first().type.jvmErasure.java
            if (!annotation.ignoreIntents && eventErasure.isSubclassOf<Event>()) {
                @Suppress("UNCHECKED_CAST")
                val requiredIntents = GatewayIntent.fromEvents(eventErasure as Class<out Event>)
                val missingIntents = requiredIntents - jdaService.intents - config.ignoredIntents - enumSetOf(*annotation.ignoredIntents)
                if (missingIntents.isNotEmpty()) {
                    return@forEach logger.debug { "Skipping event listener ${function.shortSignature} as it is missing intents: $missingIntents" }
                }

                // Cannot check for RawGatewayEvent as JDA is not present yet and there is no config for it
            }

            val eventParameters = parameters.drop(1)
                // The main risk was with injected services, as they may not be available at that point,
                // but they are pretty much limited to objects manually added by the framework, before the service loading occurs
                // In the worst case, users can request lazy services
                .onEach {
                    serviceContainer.canCreateWrappedService(it)?.let { serviceError ->
                        throwArgument(
                            classPathFunc.function,
                            "Unable to register event listener due to an unavailable service: ${serviceError.toSimpleString()}"
                        )
                    }
                }
            val eventHandlerFunction = EventHandlerFunction(
                eventType = eventErasure,
                classPathFunction = classPathFunc,
                runMode = annotation.mode,
                timeout = getTimeout(annotation),
                priority = annotation.priority,
                parametersBlock = {
                    //Getting services is delayed until execution, as to ensure late services can be used in listeners
                    eventParameters.map { serviceContainer.tryGetWrappedService(it).getOrThrow() }
                })

            // Create or update list of handlers owned by the listener
            // This is effectively the same as a CopyOnWriteArrayList, but takes advantage of the ConcurrentHashMap's locking
            listenerFunctionMap.merge(classPathFunc.instance, listOf(eventHandlerFunction), List<EventHandlerFunction>::plus)
        }

    private fun getTimeout(annotation: BEventListener): Duration? {
        if (annotation.timeout < 0) return Duration.INFINITE

        return annotation.timeout.toDuration(annotation.timeoutUnit.toDurationUnit()).let {
            it.takeIfFinite() ?: defaultTimeout.takeIfFinite()
        }
    }
}
