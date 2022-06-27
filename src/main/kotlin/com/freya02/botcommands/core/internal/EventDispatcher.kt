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
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

class EventDispatcher internal constructor(private val context: BContextImpl) {
    private val map: MutableMap<KClass<*>, MutableList<KFunction<*>>> = hashMapOf()

    init {
        for (function in context.classPathContainer
            .functionsWithAnnotation<BEventListener>()
            .requireFirstArg(GenericEvent::class, BEvent::class)) {

            val parameters = function.nonInstanceParameters
            getEventFunctionArgs(parameters) //Checks if parameters are resolved

            map.getOrPut(parameters.first().type.jvmErasure) { mutableListOf() }.add(function)
        }

        context.eventManager.listener<Event> {
            dispatchEvent(it)
        }
    }

    suspend fun dispatchEvent(event: Any) {
        when (event) {
            is GenericEvent, is BEvent -> {
                map[event::class]?.forEach { function ->
                    val parameters = function.parameters
                    val args = context.serviceContainer.getParameters(
                        parameters.map { it.type.jvmErasure },
                        mapOf(event::class to event)
                    )
                    function.callSuspend(*args.toTypedArray())
                }
            }
            else -> throwUser("Unrecognized event: ${event::class.jvmName}")
        }
    }

    private fun getEventFunctionArgs(parameters: List<KParameter>) =
        context.serviceContainer.getParameters(parameters.drop(1).map { it.type.jvmErasure })
}