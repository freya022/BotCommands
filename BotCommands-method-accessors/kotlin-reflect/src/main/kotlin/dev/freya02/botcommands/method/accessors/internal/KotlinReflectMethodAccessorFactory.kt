package dev.freya02.botcommands.method.accessors.internal

import dev.freya02.botcommands.method.accessors.internal.invoker.default.KotlinReflectDefaultMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.invoker.default.KotlinReflectDefaultStaticMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.invoker.direct.JavaReflectDirectMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.invoker.direct.KotlinReflectDirectMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.invoker.direct.KotlinReflectDirectStaticMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.utils.isInnerClass
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

class KotlinReflectMethodAccessorFactory : MethodAccessorFactory {

    override fun <R> create(
        instance: Any?,
        function: KFunction<R>,
    ): MethodAccessor<R> {
        val hasOptionals = function.parameters.any { it.isOptional }

        val requiresInstance = when (val executable = function.javaMethod ?: function.javaConstructor) {
            is Method -> !Modifier.isStatic(executable.modifiers)
            // For inner class constructors, pass the outer class as the instance parameter
            is Constructor<*> -> executable.declaringClass.isInnerClass
            else -> error("Could not get executable from $function")
        }
        return if (requiresInstance) {
            requireNotNull(instance)

            if (hasOptionals) {
                KotlinReflectDefaultMethodAccessor(instance, function)
            } else if (function.isSuspend || function.hasValueClass()) {
                KotlinReflectDirectMethodAccessor(instance, function)
            } else { // Vanilla Java method, don't use kotlin-reflect
                val executable = function.javaMethod
                    ?: function.javaConstructor
                    ?: return KotlinReflectDirectMethodAccessor(instance, function)
                JavaReflectDirectMethodAccessor(instance, executable)
            }
        } else {
            if (hasOptionals) {
                KotlinReflectDefaultStaticMethodAccessor(function)
            } else if (function.isSuspend || function.hasValueClass()) {
                KotlinReflectDirectStaticMethodAccessor(function)
            } else { // Vanilla Java method, don't use kotlin-reflect
                val executable = function.javaMethod
                    ?: function.javaConstructor
                    ?: return KotlinReflectDirectStaticMethodAccessor(function)
                JavaReflectDirectMethodAccessor(null, executable)
            }
        }
    }

    private fun KFunction<*>.hasValueClass(): Boolean {
        return parameters.any { it.type.jvmErasure.isValue } || returnType.jvmErasure.isValue
    }
}
