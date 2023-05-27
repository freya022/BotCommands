package com.freya02.botcommands.internal.core.reflection

import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

sealed class Function<R>(kFunction: KFunction<R>) {
    val kFunction = kFunction.reflectReference()
    val parametersSize = kFunction.parameters.size

    override fun equals(other: Any?) = kFunction == other
    override fun hashCode() = kFunction.hashCode()
}