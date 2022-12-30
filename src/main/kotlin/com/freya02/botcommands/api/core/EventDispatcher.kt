package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.Logging
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
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure

private typealias EventMap = MutableMap<KClass<*>, MutableList<PreboundFunction>>

class EventDispatcher internal constructor(private val context: BContextImpl, private val eventTreeService: EventTreeService) {
    private val logger = Logging.getLogger()

    private val map: EventMap = hashMapOf()
    private val listeners: MutableMap<Any, EventMap> = hashMapOf()

    init {
        context.serviceContainer.putService(this)

        addEventListeners(null, context.classPathContainer.functionsWithAnnotation<BEventListener>())

        context.eventManager.listener<Event> {
            dispatchEvent(it)
        }
    }

    fun addEventListener(listener: Any) {
        addEventListeners(
            listener,
            listener::class
                .functions
                .filterWithAnnotation<BEventListener>()
                .toClassPathFunctions(listener)
        )
    }

    fun removeEventListener(listener: Any) {
        listeners[listener]?.let { instanceMap ->
            instanceMap.forEach { (kClass, functions) ->
                val functionMap = map[kClass]
                    ?: throwInternal("Listener was registered without having its functions added to the listener map")
                functionMap.removeAll(functions)
            }
        }
    }

    suspend fun dispatchEvent(event: Any) {
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = map[event::class] ?: return

        handlers.forEach { preboundFunction -> runEventHandler(preboundFunction, event) }
    }

    suspend fun dispatchEventAsync(event: Any): List<Deferred<Unit>> {
        // Try not to switch context on non-handled events
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = map[event::class] ?: return emptyList()

        val scope = context.config.coroutineScopesConfig.eventDispatcherScope
        return handlers.map { preboundFunction ->
            scope.async { runEventHandler(preboundFunction, event) }
        }
    }

    private suspend fun runEventHandler(
        preboundFunction: PreboundFunction,
        event: Any
    ) {
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

    private fun addEventListeners(instance: Any?, functions: List<ClassPathFunction>) = functions
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
            instance?.let { instance ->
                //Skip adding event listeners if the instance is already registered
                if (listeners[instance] != null) return

                val instanceMap: EventMap = hashMapOf()
                instanceMap.getOrPut(eventErasure) { mutableListOf() }.add(preboundFunction)

                listeners[instance] = instanceMap
            }

            (eventTreeService.getSubclasses(eventErasure) + eventErasure).forEach {
                map.getOrPut(it) { mutableListOf() }.add(preboundFunction)
            }
        }

    private fun printException(preboundFunction: PreboundFunction, e: Throwable) = logger.error(
        "An exception occurred while dispatching an event for ${preboundFunction.classPathFunction.function.shortSignature}",
        e.getDeepestCause()
    )
}