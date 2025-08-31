package dev.freya02.botcommands.method.accessors.internal

import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter

class KotlinReflectMethodAccessorFactory : MethodAccessorFactory {

    override fun <R> create(
        instance: Any?,
        function: KFunction<R>,
    ): MethodAccessor<R> {
        return if (function.instanceParameter != null) {
            requireNotNull(instance)

            KotlinReflectMethodAccessor(instance, function)
        } else {
            KotlinReflectStaticMethodAccessor(function)
        }
    }
}
