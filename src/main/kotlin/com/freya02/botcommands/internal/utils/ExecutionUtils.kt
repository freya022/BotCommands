package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KParameter

operator fun MutableMap<KParameter, Any?>.set(parameter: MethodParameter, obj: Any?): Any? = obj.also {
    this[parameter.kParameter] = obj
}

@Suppress("UNCHECKED_CAST")
operator fun MutableMap<KParameter, Any?>.set(option: Option, obj: Any?): Any? = obj.also {
    if (option.isVararg) {
        (this.getOrPut(option.executableParameter) {
            arrayListOf<Any?>()
        } as MutableList<Any?>).add(obj)
    } else {
        this[option.executableParameter] = obj
    }
}