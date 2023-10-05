package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

sealed class Function<R>(boundFunction: KFunction<R>) {
    val kFunction = boundFunction.reflectReference()
    val parametersSize = kFunction.parameters.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Function<*>

        return kFunction == other.kFunction
    }

    override fun hashCode() = kFunction.hashCode()
}