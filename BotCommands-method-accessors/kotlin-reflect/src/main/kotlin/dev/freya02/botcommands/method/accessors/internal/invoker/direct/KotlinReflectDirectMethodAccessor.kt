package dev.freya02.botcommands.method.accessors.internal.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.AbstractKotlinReflectMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import dev.freya02.botcommands.method.accessors.internal.exceptions.IllegalSuspendCallException
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

internal class KotlinReflectDirectMethodAccessor<R> internal constructor(
    private val instance: Any,
    function: KFunction<R>,
) : AbstractKotlinReflectMethodAccessor<R>(function) {

    override fun hasInstance(): Boolean = true

    override suspend fun callSuspend(args: MethodArguments): R {
        return function.callSuspend(instance, *args.args)
    }

    override fun call(args: MethodArguments): R {
        if (function.isSuspend) throw IllegalSuspendCallException()

        return function.call(instance, *args.args)
    }
}
