package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KParameter

operator fun MutableMap<KParameter, Any?>.set(parameter: MethodParameter, obj: Any?): Any? = obj.also {
    this[parameter.kParameter] = obj
}

fun Map<KParameter, Any?>.expandVararg() = this

@Suppress("UNCHECKED_CAST")
operator fun MutableMap<KParameter, Any?>.set(parameter: Option, obj: Any?): Any? = obj.also {
    if (parameter.isVararg) {
        (this.getOrPut(parameter.executableParameter) {
            arrayListOf<Any?>()
        } as MutableList<Any?>).add(obj)
    } else {
        this[parameter.executableParameter] = obj
    }
}