package dev.freya02.botcommands.method.accessors.internal

import dev.freya02.botcommands.method.accessors.internal.codegen.ClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.utils.isInnerClass
import dev.freya02.botcommands.method.accessors.internal.utils.javaExecutable
import java.lang.invoke.MethodHandles
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction

class ClassFileMethodAccessorFactory : MethodAccessorFactory {

    private val lookup = MethodHandles.lookup()

    override fun <R> create(
        instance: Any?,
        function: KFunction<R>,
    ): MethodAccessor<R> {
        val executable = function.javaExecutable
        // Require an instance for non-static methods or inner class constructors
        if (executable is Method && !Modifier.isStatic(executable.modifiers)) {
            requireNotNull(instance)
            require(executable.declaringClass.isAssignableFrom(instance.javaClass)) {
                "Function is not from the instance's class, function: ${executable.declaringClass.name}, instance: ${instance.javaClass.name}"
            }
        } else if (executable is Constructor<*> && executable.declaringClass.isInnerClass) {
            requireNotNull(instance)
            val outerClass = executable.declaringClass.declaringClass
            require(outerClass.isAssignableFrom(instance.javaClass)) {
                "Passed instance is not assignable to the outer class of this inner class constructor, required: ${outerClass.name}, instance: ${instance.javaClass.name}"
            }
        }

        return ClassFileMethodAccessorGenerator.generate(instance, function, lookup)
    }
}
