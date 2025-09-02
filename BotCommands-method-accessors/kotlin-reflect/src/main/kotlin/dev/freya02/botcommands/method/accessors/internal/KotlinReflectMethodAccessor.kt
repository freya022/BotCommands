package dev.freya02.botcommands.method.accessors.internal

import dev.freya02.botcommands.method.accessors.internal.exceptions.IllegalSuspendCallException
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter

internal class KotlinReflectMethodAccessor<R> internal constructor(
    private val instance: Any,
    function: KFunction<R>,
) : AbstractKotlinReflectMethodAccessor<R>(function) {

    private val instanceParameter = function.instanceParameter!!

    override fun hasInstance(): Boolean = true

    override suspend fun callSuspend(args: MethodArguments): R {
        val args = argsToMap(args) {
            put(instanceParameter, instance)
        }

        return function.callSuspendBy(args)
    }

    override fun call(args: MethodArguments): R {
        if (function.isSuspend) throw IllegalSuspendCallException()

        val args = argsToMap(args) {
            put(instanceParameter, instance)
        }

        return function.callBy(args)
    }
}
