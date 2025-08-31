package dev.freya02.botcommands.method.accessors.internal

import io.github.freya022.botcommands.method.accessors.internal.MethodAccessor
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter

internal class KotlinReflectMethodAccessor<R> internal constructor(
    private val instance: Any,
    private val function: KFunction<R>,
) : MethodAccessor<R> {

    private val instanceParameter = function.instanceParameter

    override suspend fun call(args: Map<KParameter, Any?>): R {
        val args = args.toMutableMap()
        if (instanceParameter != null) args.putIfAbsent(instanceParameter, instance)

        return function.callSuspendBy(args)
    }
}
