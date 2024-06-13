package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal sealed class Function<R>(boundFunction: KFunction<R>) {
    internal val kFunction = boundFunction.reflectReference()
    internal val parametersSize = kFunction.parameters.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Function<*>

        return kFunction == other.kFunction
    }

    override fun hashCode() = kFunction.hashCode()
}