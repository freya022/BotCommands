package dev.freya02.botcommands.method.accessors.internal.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import dev.freya02.botcommands.method.accessors.internal.utils.isInnerClass
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method

internal class JavaReflectDirectMethodAccessor<R> internal constructor(
    private val instance: Any?,
    private val executable: Executable,
) : MethodAccessor<R> {

    // Java treats outer class as an argument, but we always pass it as the instance
    private val argumentCount = executable.parameterCount - if (executable is Constructor<*> && executable.declaringClass.isInnerClass) 1 else 0

    override fun hasInstance(): Boolean {
        return instance != null
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun callSuspend(args: MethodArguments): R {
        return when (executable) {
            is Constructor<*> if instance != null -> executable.newInstance(instance, *args.args)
            is Constructor<*> -> executable.newInstance(*args.args)
            is Method -> executable.invoke(instance, *args.args)
        } as R
    }

    @Suppress("UNCHECKED_CAST")
    override fun call(args: MethodArguments): R {
        return when (executable) {
            is Constructor<*> if instance != null -> executable.newInstance(instance, *args.args)
            is Constructor<*> -> executable.newInstance(*args.args)
            is Method -> executable.invoke(instance, *args.args)
        } as R
    }

    override fun createBlankArguments(): MethodArguments {
        return MethodArguments(argumentCount)
    }
}
