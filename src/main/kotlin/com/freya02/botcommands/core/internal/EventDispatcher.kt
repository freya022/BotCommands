package com.freya02.botcommands.core.internal

import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.events.BEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

class EventDispatcher internal constructor(context: BContextImpl) {
    private val map: MutableMap<KClass<*>, MutableList<EventListenerFunction>> = hashMapOf()

    init {
        for (classPathFunc in context.classPathContainer
            .functionsWithAnnotation<BEventListener>()
            .requireNonStatic()
            .requireFirstArg(GenericEvent::class, BEvent::class)) {

            val function = classPathFunc.function

            val parameters = function.nonInstanceParameters
            val args = context.serviceContainer.getParameters(
                parameters.drop(1).map { it.type.jvmErasure }
            )
            map.getOrPut(parameters.first().type.jvmErasure) { mutableListOf() }.add(EventListenerFunction(classPathFunc.instance, function, args.toTypedArray()))
        }

        context.eventManager.listener<Event> {
            dispatchEvent(it)
        }
    }

    suspend fun dispatchEvent(event: Any) {
        when (event) {
            is GenericEvent, is BEvent -> {
                map[event::class]?.forEach { eventListener ->
                    val function = eventListener.function

                    function.callSuspend(eventListener.instance, *eventListener.parameters, event)
                }
            }
            else -> throwUser("Unrecognized event: ${event::class.jvmName}")
        }
    }

    inner class EventListenerFunction(val instance: Any, val function: KFunction<*>, val parameters: Array<Any>)
}