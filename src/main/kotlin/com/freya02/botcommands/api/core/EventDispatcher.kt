package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.BEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.EventHandlerFunction
import com.freya02.botcommands.internal.core.exceptions.InitializationException
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.core.service.FunctionAnnotationsMap
import com.freya02.botcommands.internal.core.toClassPathFunctions
import com.freya02.botcommands.internal.utils.*
import com.freya02.botcommands.internal.utils.ReflectionUtils.declaringClass
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import dev.minn.jda.ktx.events.CoroutineEventManager
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

// https://discord.com/channels/125227483518861312/125227483518861312/1114953133722980453
internal class ConcurrentTreeSet<T> {
    // Only protect modification operations, traversal is fine
    private val lock = ReentrantLock()
    private var set: MutableSet<T> = TreeSet()

    fun add(t: T): Boolean = lock.withLock {
        val newSet = TreeSet(set)
        return newSet.add(t).also {
            this.set = newSet
        }
    }

    fun remove(t: T): Boolean = lock.withLock {
        val newSet = TreeSet(set)
        return newSet.remove(t).also {
            this.set = newSet
        }
    }

    inline fun <R> map(block: (T) -> R): List<R> = set.map(block)

    fun removeAll(removedSet: ConcurrentTreeSet<T>): Boolean = lock.withLock {
        val newSet = TreeSet(set)
        return newSet.removeAll(removedSet.set).also {
            this.set = newSet
        }
    }

    inline fun forEach(block: (T) -> Unit) = set.forEach(block)
}

private typealias EventMap = MutableMap<KClass<*>, ConcurrentTreeSet<EventHandlerFunction>>

@BService
class EventDispatcher internal constructor(
    private val context: BContextImpl,
    private val eventTreeService: EventTreeService,
    functionAnnotationsMap: FunctionAnnotationsMap
) {
    private val logger = KotlinLogging.logger { }
    private val eventManager: CoroutineEventManager = context.eventManager

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
        listeners[listener::class.java]?.let { instanceMap ->
            instanceMap.forEach { (kClass, functions) ->
                val functionMap = map[kClass]
                    ?: throwInternal("Listener was registered without having its functions added to the listener map")
                functionMap.removeAll(functions)
            }
        }
    }

    @JvmSynthetic
    suspend fun dispatchEvent(event: Any) {
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = map[event::class] ?: return

        handlers.forEach { preboundFunction ->
            if (preboundFunction.isAsync) {
                context.coroutineScopesConfig.eventDispatcherScope.launch {
                    runEventHandler(preboundFunction, event)
                }
            } else {
                runEventHandler(preboundFunction, event)
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
        return handlers.map { preboundFunction ->
            scope.async { runEventHandler(preboundFunction, event) }
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
                else -> printException(eventHandlerFunction, e)
            }
        } catch (e: Throwable) {
            printException(eventHandlerFunction, e)
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
                    instanceMap.computeIfAbsent(it) { ConcurrentTreeSet() }.add(eventHandlerFunction)
                }
            }

            (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                map.computeIfAbsent(it) { ConcurrentTreeSet() }.add(eventHandlerFunction)
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

    private fun printException(eventHandlerFunction: EventHandlerFunction, e: Throwable) = logger.error(
        "An exception occurred while dispatching an event for ${eventHandlerFunction.classPathFunction.function.shortSignature}",
        e.unwrap()
    )
}
