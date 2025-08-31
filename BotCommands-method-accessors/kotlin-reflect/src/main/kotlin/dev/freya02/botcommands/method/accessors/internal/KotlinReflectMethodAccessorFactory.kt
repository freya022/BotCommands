package dev.freya02.botcommands.method.accessors.internal

import kotlin.reflect.KFunction

class KotlinReflectMethodAccessorFactory : MethodAccessorFactory {

    override fun <R> create(
        instance: Any,
        function: KFunction<R>,
    ): MethodAccessor<R> = KotlinReflectMethodAccessor(instance, function)
}
