package dev.freya02.botcommands.method.accessors.internal.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method

internal class JavaReflectDirectMethodAccessor<R> internal constructor(
    private val instance: Any?,
    private val method: Executable,
) : MethodAccessor<R> {

    override fun hasInstance(): Boolean {
        return instance != null
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun callSuspend(args: MethodArguments): R {
        return when (method) {
            is Constructor<*> -> method.newInstance(*args.args)
            is Method -> method.invoke(instance, *args.args)
        } as R
    }

    @Suppress("UNCHECKED_CAST")
    override fun call(args: MethodArguments): R {
        return when (method) {
            is Constructor<*> -> method.newInstance(*args.args)
            is Method -> method.invoke(instance, *args.args)
        } as R
    }

    override fun createBlankArguments(): MethodArguments {
        return MethodArguments(method.parameterCount)
    }
}
