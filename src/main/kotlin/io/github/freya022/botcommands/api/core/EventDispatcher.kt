package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.BEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.*
import io.github.freya022.botcommands.internal.core.exceptions.InitializationException
import io.github.freya022.botcommands.internal.core.exceptions.InternalException
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.core.service.getParameters
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.shortSignature
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

internal class SortedList<T>(private val comparator: Comparator<T>) {
    // Only protect modification operations, traversal is fine
    private val lock = ReentrantLock()
    private var list: List<T> = arrayListOf()

    fun add(t: T): Unit = lock.withLock {
        val newList = list + t
        this.list = newList.sortedWith(comparator)
    }

    fun remove(t: T): Boolean = lock.withLock {
        val newList = list.toMutableList()
        newList.remove(t).also {
            this.list = newList
        }
    }

    inline fun <R> map(block: (T) -> R): List<R> = list.map(block)

    fun removeAll(removedList: SortedList<T>): Boolean = lock.withLock {
        val newList = list.toMutableList()
        return newList.removeAll(removedList.list).also {
            this.list = newList
        }
    }

    inline fun forEach(block: (T) -> Unit) = list.forEach(block)
}

private typealias EventMap = MutableMap<KClass<*>, SortedList<EventHandlerFunction>>

@BService
class EventDispatcher internal constructor(
    private val context: BContextImpl,
    private val eventTreeService: EventTreeService,
    functionAnnotationsMap: FunctionAnnotationsMap
) {
    private val logger = KotlinLogging.logger { }
    private val eventManager: CoroutineEventManager = context.eventManager
    private val jdaService: JDAService? = context.getServiceOrNull()

    private val map: EventMap = ConcurrentHashMap()
    private val listeners: MutableMap<Class<*>, EventMap> = ConcurrentHashMap()

    init {
        functionAnnotationsMap
            .getFunctionsWithAnnotation<BEventListener>()
            .addAsEventListeners()

        //This could dispatch to multiple listeners, timeout must be handled on a per-listener basis manually
        // as jda-ktx takes this group of listeners as only being one.
        eventManager.listener<Event>(timeout = Duration.INFINITE) {
            dispatchEvent(it)
        }
    }

    fun addEventListener(listener: Any) {
        listener::class
            .functions
            .withFilter(FunctionFilter.annotation<BEventListener>())
            .toClassPathFunctions(listener)
            .addAsEventListeners()
    }

    fun removeEventListener(listener: Any) {
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

    @JvmSynthetic
    suspend fun dispatchEvent(event: Any) {
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = map[event::class] ?: return

        handlers.forEach { eventHandler ->
            if (eventHandler.isAsync) {
                context.coroutineScopesConfig.eventDispatcherScope.launch {
                    runEventHandler(eventHandler, event)
                }
            } else {
                runEventHandler(eventHandler, event)
            }
        }
    }

    @JvmName("dispatchEvent")
    fun dispatchEventJava(event: Any) = runBlocking { dispatchEvent(event) }

    fun dispatchEventAsync(event: Any): List<Deferred<Unit>> {
        // Try not to switch context on non-handled events
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = map[event::class] ?: return emptyList()

        val scope = context.coroutineScopesConfig.eventDispatcherScope
        return handlers.map { eventHandler ->
            scope.async { runEventHandler(eventHandler, event) }
        }
    }

    private suspend fun runEventHandler(eventHandlerFunction: EventHandlerFunction, event: Any) {
        try {
            val (instance, function) = eventHandlerFunction.classPathFunction

            /**
             * See [CoroutineEventManager.handle]
             */
            val actualTimeout = eventHandlerFunction.timeout
            if (actualTimeout.isPositive() && actualTimeout.isFinite()) {
                // Timeout only works when the continuations implement a cancellation handler
                val result = withTimeoutOrNull(actualTimeout.inWholeMilliseconds) {
                    function.callSuspend(instance, event, *eventHandlerFunction.parameters)
                }
                if (result == null) {
                    logger.debug("Event of type ${event.javaClass.simpleName} timed out.")
                }
            } else {
                function.callSuspend(instance, event, *eventHandlerFunction.parameters)
            }
        } catch (e: InvocationTargetException) {
            when (e.cause) {
                is InitializationException -> throw e.cause!!
                else -> printException(event, eventHandlerFunction, e)
            }
        } catch (e: Throwable) {
            printException(event, eventHandlerFunction, e)
        }
    }

    private fun Collection<ClassPathFunction>.addAsEventListeners() = this
        .requiredFilter(FunctionFilter.nonStatic())
        .requiredFilter(FunctionFilter.firstArg(GenericEvent::class, BEvent::class))
        .forEach { classPathFunc ->
            val function = classPathFunc.function
            val annotation = function.findAnnotation<BEventListener>()
                ?: throwInternal(function, "Function was asserted to have BEventListener but it was not found")

            val parameters = function.nonInstanceParameters

            val eventErasure = parameters.first().type.jvmErasure
            if (!annotation.ignoreIntents && jdaService != null && eventErasure.isSubclassOf(Event::class)) {
                @Suppress("UNCHECKED_CAST")
                val requiredIntents = GatewayIntent.fromEvents(eventErasure.java as Class<out Event>)
                val missingIntents = requiredIntents - jdaService.intents - context.config.ignoredIntents
                if (missingIntents.isNotEmpty()) {
                    return@forEach logger.debug { "Skipping event listener ${function.shortSignature} as it is missing intents: $missingIntents" }
                }
            }

            val eventParametersErasures = parameters.drop(1).map { it.type.jvmErasure }
                // The main risk was with injected services, as they may not be available at that point,
                // but they are pretty much limited to objects manually added by the framework, before the service loading occurs
                .onEach {
                    context.serviceContainer.canCreateService(it)?.let { errorMessage ->
                        throwUser(
                            classPathFunc.function,
                            "Unable to register event listener due to an unavailable service: $errorMessage"
                        )
                    }
                }
            val eventHandlerFunction = EventHandlerFunction(classPathFunction = classPathFunc,
                isAsync = annotation.async,
                timeout = getTimeout(annotation),
                priority = annotation.priority,
                parametersBlock = {
                    //Getting services is delayed until execution, as to ensure late services can be used in listeners
                    context.serviceContainer.getParameters(eventParametersErasures).toTypedArray()
                })

            classPathFunc.function.declaringClass.java.let { clazz ->
                val instanceMap = listeners.computeIfAbsent(clazz) { hashMapOf() }

                (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                    instanceMap.computeIfAbsent(it) { SortedList(EventHandlerFunction.priorityComparator) }.add(eventHandlerFunction)
                }
            }

            (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                map.computeIfAbsent(it) { SortedList(EventHandlerFunction.priorityComparator) }.add(eventHandlerFunction)
            }
        }

    private fun getTimeout(annotation: BEventListener): Duration {
        if (annotation.timeout < 0) return Duration.INFINITE

        return annotation.timeout.toDuration(annotation.timeoutUnit.toDurationUnit()).let {
            when {
                it.isPositive() && it.isFinite() -> it
                else -> eventManager.timeout
            }
        }
    }

    private fun printException(event: Any, eventHandlerFunction: EventHandlerFunction, e: Throwable) = logger.error(
        "An exception occurred while dispatching a ${event.javaClass.simpleNestedName} for ${eventHandlerFunction.classPathFunction.function.shortSignature}",
        e.unwrap()
    )
}
