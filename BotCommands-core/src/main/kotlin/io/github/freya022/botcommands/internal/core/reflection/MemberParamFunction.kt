package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.service.getFunctionService
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

class MemberParamFunction<T : Any, R>(
    boundFunction: KFunction<R>,
    instanceSupplier: () -> Any,
    paramClass: KClass<T>
) : MemberFunction<R>(boundFunction, instanceSupplier) {
    init {
        requireAt(firstParameter.type.jvmErasure.isSubclassOf(paramClass), kFunction) {
            "First argument should be a ${paramClass.simpleNestedName}"
        }
    }

    constructor(context: BContext, boundFunction: KFunction<R>, paramClass: KClass<T>) : this(
        boundFunction = boundFunction,
        instanceSupplier = { context.serviceContainer.getFunctionService(boundFunction) },
        paramClass = paramClass
    )
}

inline fun <reified T : Any> ClassPathFunction.toMemberParamFunction() =
    MemberParamFunction(function, instanceSupplier = { instance }, T::class)

internal inline fun <reified T : Any, R> KFunction<R>.toMemberParamFunction(context: BContext) =
    MemberParamFunction(context, this, T::class)

fun <T : Any, R> KFunction<R>.toMemberParamFunction(context: BContext, paramType: KClass<T>) =
    MemberParamFunction(context, this, paramType)

inline fun <reified T : Any, R> IBuilderFunctionHolder<R>.toMemberParamFunction(context: BContext): MemberParamFunction<T, R> {
    return MemberParamFunction(context, this.function, T::class)
}
