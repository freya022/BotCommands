package io.github.freya022.botcommands.internal.core.reflection

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.method.accessors.MethodAccessorFactoryProvider
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KFunction

internal open class MemberFunction<R> internal constructor(
    boundFunction: KFunction<R>,
    instanceSupplier: () -> Any
) : Function<R>(boundFunction) {
    val instance by lazy(instanceSupplier)
    val methodAccessor: MethodAccessor<R> by lazy { MethodAccessorFactoryProvider.getAccessorFactory().create(instance, kFunction) }

    val firstParameter = kFunction.nonInstanceParameters.firstOrNull()
        ?: throwInternal(kFunction, "The function should have been checked to have at least one parameter")
}

internal fun ClassPathFunction.toMemberFunction() = MemberFunction(function, instanceSupplier = { this.instance })
