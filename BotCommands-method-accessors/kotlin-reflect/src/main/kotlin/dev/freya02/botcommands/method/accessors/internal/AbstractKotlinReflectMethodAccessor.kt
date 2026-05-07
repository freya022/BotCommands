package dev.freya02.botcommands.method.accessors.internal

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal abstract class AbstractKotlinReflectMethodAccessor<R>(
    protected val function: KFunction<R>,
) : MethodAccessor<R> {

    private val parameterCount = function.parameters.count { it.kind != KParameter.Kind.INSTANCE }

    override fun createBlankArguments(): MethodArguments {
        return MethodArguments(parameterCount)
    }
}
