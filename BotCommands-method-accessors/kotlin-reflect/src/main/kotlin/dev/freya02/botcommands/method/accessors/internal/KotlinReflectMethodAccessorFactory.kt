package dev.freya02.botcommands.method.accessors.internal

import dev.freya02.botcommands.method.accessors.internal.invoker.default.KotlinReflectDefaultMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.invoker.default.KotlinReflectDefaultStaticMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.invoker.direct.KotlinReflectDirectMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.invoker.direct.KotlinReflectDirectStaticMethodAccessor
import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter

class KotlinReflectMethodAccessorFactory : MethodAccessorFactory {

    override fun <R> create(
        instance: Any?,
        function: KFunction<R>,
    ): MethodAccessor<R> {
        return if (function.instanceParameter != null) {
            requireNotNull(instance)

            if (function.parameters.any { it.isOptional }) {
                KotlinReflectDefaultMethodAccessor(instance, function)
            } else {
                KotlinReflectDirectMethodAccessor(instance, function)
            }
        } else {
            if (function.parameters.any { it.isOptional }) {
                KotlinReflectDefaultStaticMethodAccessor(function)
            } else {
                KotlinReflectDirectStaticMethodAccessor(function)
            }
        }
    }
}
