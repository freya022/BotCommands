package com.freya02.botcommands.internal.events

import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class EventListenerParameter(parameter: KParameter, index: Int) : CommandParameter<Any>(null, parameter, index) {
    override val optionAnnotations: List<KClass<out Annotation>>
        get() = listOf()
}