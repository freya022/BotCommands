package com.freya02.botcommands.core.internal

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.events.BEvent
import com.freya02.botcommands.core.api.exceptions.InitializationException
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.getDeepestCause
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

class EventDispatcher internal constructor(private val context: BContextImpl) {
    private val map: MutableMap<KClass<*>, MutableList<PreboundFunction>> = hashMapOf()

    init {
        context.serviceContainer.putService(this)

        for (classPathFunc in context.classPathContainer
            .functionsWithAnnotation<BEventListener>()
            .requireNonStatic()
            .requireFirstArg(GenericEvent::class, BEvent::class)) {

            val function = classPathFunc.function

            val parameters = function.nonInstanceParameters
            val args = context.serviceContainer.getParameters(
                parameters.drop(1).map { it.type.jvmErasure }
            )
            map.getOrPut(parameters.first().type.jvmErasure) { mutableListOf() }.add(PreboundFunction(classPathFunc, args.toTypedArray()))
        }

        context.eventManager.listener<Event> {
            dispatchEvent(it)
        }
    }

    suspend fun dispatchEvent(event: Any) {
        // Try not to switch context on non-handled events
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = map[event::class] ?: return

        withContext(context.config.coroutineScopesConfig.eventDispatcherScope.coroutineContext) {
            handlers.forEach { preboundFunction ->
                try {
                    val classPathFunction = preboundFunction.classPathFunction

                    classPathFunction.function.callSuspend(classPathFunction.instance, event, *preboundFunction.parameters)
                } catch (e: InvocationTargetException) {
                    when (e.cause) {
                        is InitializationException -> throw e.cause!!
                        else -> printException(preboundFunction, e)
                    }
                } catch (e: Throwable) {
                    printException(preboundFunction, e)
                }
            }
        }
    }

    private fun printException(preboundFunction: PreboundFunction, e: Throwable) = LOGGER.error(
        "An exception occurred while dispatching an event for ${preboundFunction.classPathFunction.function.shortSignature}",
        e.getDeepestCause()
    )
}