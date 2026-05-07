package dev.freya02.botcommands.method.accessors.internal.invoker.default

import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import dev.freya02.botcommands.method.accessors.internal.exceptions.IllegalSuspendCallException
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter

internal class KotlinReflectDefaultMethodAccessor<R> internal constructor(
    private val instance: Any,
    function: KFunction<R>,
) : AbstractKotlinReflectDefaultMethodAccessor<R>(function) {

    private val instanceParameter = function.instanceParameter!!

    override fun hasInstance(): Boolean = true

    override suspend fun callSuspend(args: MethodArguments): R {
        val args = argsToMap(args, instanceParameter to instance)

        return function.callSuspendBy(args)
    }

    override fun call(args: MethodArguments): R {
        if (function.isSuspend) throw IllegalSuspendCallException()

        val args = argsToMap(args, instanceParameter to instance)

        return function.callBy(args)
    }
}
