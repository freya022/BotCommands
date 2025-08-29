package dev.freya02.botcommands.method.accessors.internal

import io.github.freya022.botcommands.method.accessors.internal.MethodAccessor
import io.github.freya022.botcommands.method.accessors.internal.MethodAccessorFactory
import kotlin.reflect.KFunction

internal class KotlinReflectMethodAccessorFactory : MethodAccessorFactory {

    override val priority: Int = 0

    override fun create(
        instance: Any,
        function: KFunction<*>,
    ): MethodAccessor = KotlinReflectMethodAccessor(instance, function)
}
