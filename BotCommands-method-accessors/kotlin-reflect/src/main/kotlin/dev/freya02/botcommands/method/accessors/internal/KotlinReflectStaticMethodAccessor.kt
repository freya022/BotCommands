package dev.freya02.botcommands.method.accessors.internal

import dev.freya02.botcommands.method.accessors.internal.exceptions.IllegalSuspendCallException
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspendBy

internal class KotlinReflectStaticMethodAccessor<R> internal constructor(
    function: KFunction<R>,
) : AbstractKotlinReflectMethodAccessor<R>(function) {

    override fun hasInstance(): Boolean = false

    override suspend fun callSuspend(args: MethodArguments): R {
        return function.callSuspendBy(argsToMap(args))
    }

    override fun call(args: MethodArguments): R {
        if (function.isSuspend) throw IllegalSuspendCallException()
        return function.callBy(argsToMap(args))
    }
}
