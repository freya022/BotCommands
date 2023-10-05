package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

open class MemberFunction<R> internal constructor(
    boundFunction: KFunction<R>,
    instanceSupplier: () -> Any
) : Function<R>(boundFunction) {
    val instance by lazy(instanceSupplier)

    val resolvableParameters = kFunction.valueParameters.drop(1) //Drop the first parameter
    val instanceParameter = kFunction.instanceParameter
        ?: throwInternal(kFunction, "Function shouldn't be static or constructors")
    val firstParameter = kFunction.nonInstanceParameters.firstOrNull()
        ?: throwInternal(kFunction, "The function should have been checked to have at least one parameter")
}

internal fun ClassPathFunction.toMemberFunction() = MemberFunction(function, instanceSupplier = { this.instance })