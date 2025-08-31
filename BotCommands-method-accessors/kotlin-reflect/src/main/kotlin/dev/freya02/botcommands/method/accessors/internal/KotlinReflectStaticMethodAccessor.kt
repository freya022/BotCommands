package dev.freya02.botcommands.method.accessors.internal

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy

internal class KotlinReflectStaticMethodAccessor<R> internal constructor(
    private val function: KFunction<R>,
) : MethodAccessor<R> {

    override suspend fun call(args: Map<KParameter, Any?>): R {
        return function.callSuspendBy(args)
    }
}
