package dev.freya02.botcommands.method.accessors.internal

import dev.freya02.botcommands.method.accessors.internal.codegen.ClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.utils.javaExecutable
import io.github.freya022.botcommands.method.accessors.internal.MethodAccessor
import io.github.freya022.botcommands.method.accessors.internal.MethodAccessorFactory
import java.lang.invoke.MethodHandles
import kotlin.reflect.KFunction

class ClassFileMethodAccessorFactory : MethodAccessorFactory {

    private val lookup = MethodHandles.lookup()

    override fun <R> create(
        instance: Any,
        function: KFunction<R>,
    ): MethodAccessor<R> {
        val executable = function.javaExecutable
        require(executable.declaringClass.isAssignableFrom(instance.javaClass)) {
            "Function is not from the instance's class, function: ${executable.declaringClass.name}, instance: ${instance.javaClass.name}"
        }

        return ClassFileMethodAccessorGenerator.generate(instance, function, lookup)
    }
}
