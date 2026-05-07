package dev.freya02.botcommands.method.accessors.internal.invoker.default

import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import dev.freya02.botcommands.method.accessors.internal.exceptions.IllegalSuspendCallException
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspendBy

internal class KotlinReflectDefaultStaticMethodAccessor<R> internal constructor(
    function: KFunction<R>,
) : AbstractKotlinReflectDefaultMethodAccessor<R>(function) {

    override fun hasInstance(): Boolean = false

    override suspend fun callSuspend(args: MethodArguments): R {
        return function.callSuspendBy(argsToMap(args, instanceArg = null))
    }

    override fun call(args: MethodArguments): R {
        if (function.isSuspend) throw IllegalSuspendCallException()
        return function.callBy(argsToMap(args, instanceArg = null))
    }
}
