package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.BEvent
import com.freya02.botcommands.api.core.exceptions.InitializationException
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ClassPathContainer.Companion.filterWithAnnotation
import com.freya02.botcommands.internal.core.ClassPathContainer.Companion.toClassPathFunctions
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.PreboundFunction
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.getDeepestCause
import com.freya02.botcommands.internal.javaMethodInternal
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure

private typealias EventMap = MutableMap<KClass<*>, MutableList<PreboundFunction>>

class EventDispatcher internal constructor(private val context: BContextImpl, private val eventTreeService: EventTreeService) {
    private val logger = KotlinLogging.logger { }

    private val map: EventMap = hashMapOf()
    private val listeners: MutableMap<Class<*>, EventMap> = hashMapOf()

    init {
        context.serviceContainer.putService(this)

        addEventListeners(context.classPathContainer.functionsWithAnnotation<BEventListener>())

        context.eventManager.listener<Event> {
            dispatchEvent(it)
        }
    }

    fun addEventListener(listener: Any) {
        addEventListeners(
            listener::class
                .functions
                .filterWithAnnotation<BEventListener>()
                .toClassPathFunctions(listener)
        )
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

        handlers.forEach { preboundFunction -> runEventHandler(preboundFunction, event) }
    }

    @JvmName("dispatchEvent")
    fun dispatchEventJava(event: Any) = runBlocking { dispatchEvent(event) }

    fun dispatchEventAsync(event: Any): List<Deferred<Unit>> {
        // Try not to switch context on non-handled events
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = map[event::class] ?: return emptyList()

        val scope = context.config.coroutineScopesConfig.eventDispatcherScope
        return handlers.map { preboundFunction ->
            scope.async { runEventHandler(preboundFunction, event) }
        }
    }

    private suspend fun runEventHandler(preboundFunction: PreboundFunction, event: Any) {
        try {
            val (instance, function) = preboundFunction.classPathFunction

            function.callSuspend(instance, event, *preboundFunction.parameters)
        } catch (e: InvocationTargetException) {
            when (e.cause) {
                is InitializationException -> throw e.cause!!
                else -> printException(preboundFunction, e)
            }
        } catch (e: Throwable) {
            printException(preboundFunction, e)
        }
    }

    private fun addEventListeners(functions: List<ClassPathFunction>) = functions
        .requireNonStatic()
        .requireFirstArg(GenericEvent::class, BEvent::class)
        .forEach { classPathFunc ->
            val function = classPathFunc.function

            val parameters = function.nonInstanceParameters

            val eventErasure = parameters.first().type.jvmErasure
            val eventParametersErasures = parameters.drop(1).map { it.type.jvmErasure }
//                .onEach { //Cannot predetermine availability of services when the framework is initializing as services may be injected and others might depend on those
//                    context.serviceContainer.canCreateService(it)?.let { errorMessage ->
//                        throwUser(
//                            classPathFunc.function,
//                            "Unable to register event listener due to an unavailable service: $errorMessage"
//                        )
//                    }
//                }
            val preboundFunction = PreboundFunction(classPathFunc) {
                //Getting services is delayed until execution, as to ensure late services can be used in listeners
                context.serviceContainer.getParameters(eventParametersErasures).toTypedArray()
            }

            classPathFunc.function.javaMethodInternal.declaringClass.let { clazz: Class<*> ->
                val instanceMap = listeners.computeIfAbsent(clazz) { hashMapOf() }

                (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                    instanceMap.getOrPut(it) { mutableListOf() }.add(preboundFunction)
                }
            }

            (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                map.getOrPut(it) { CopyOnWriteArrayList() }.add(preboundFunction)
            }
        }

    private fun printException(preboundFunction: PreboundFunction, e: Throwable) = logger.error(
        "An exception occurred while dispatching an event for ${preboundFunction.classPathFunction.function.shortSignature}",
        e.getDeepestCause()
    )
}