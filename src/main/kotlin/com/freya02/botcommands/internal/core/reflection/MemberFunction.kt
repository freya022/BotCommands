package com.freya02.botcommands.internal.core.reflection

import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

open class MemberFunction<R> private constructor(
    boundFunction: KFunction<R>,
    instanceSupplier: () -> Any,
    val firstParameter: KParameter
) : Function<R>(boundFunction) {
    val instance by lazy(instanceSupplier)

    val resolvableParameters = kFunction.valueParameters.drop(1) //Drop the first parameter
    val instanceParameter = kFunction.instanceParameter
        ?: throwInternal(kFunction, "Function shouldn't be static or constructors")

    internal constructor(function: KFunction<R>, instanceSupplier: () -> Any) : this(
        boundFunction = function,
        instanceSupplier = instanceSupplier,
        firstParameter = function.nonInstanceParameters.firstOrNull()
            ?: throwInternal(function, "The function should have been checked to have at least one parameter")
    )
}

internal fun ClassPathFunction.toMemberFunction() = MemberFunction(function, instanceSupplier = { this.instance })