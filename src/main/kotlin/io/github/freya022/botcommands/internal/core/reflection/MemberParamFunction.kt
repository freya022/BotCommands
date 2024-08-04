package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.builder.ExecutableCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.internal.commands.text.builder.TextCommandVariationBuilderImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.service.getFunctionService
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal class MemberParamFunction<T : Any, R> internal constructor(
    boundFunction: KFunction<R>,
    instanceSupplier: () -> Any,
    paramClass: KClass<T>
) : MemberFunction<R>(boundFunction, instanceSupplier) {
    init {
        requireAt(firstParameter.type.jvmErasure.isSubclassOf(paramClass), kFunction) {
            "First argument should be a ${paramClass.simpleNestedName}"
        }
    }

    internal constructor(context: BContext, boundFunction: KFunction<R>, paramClass: KClass<T>) : this(
        boundFunction = boundFunction,
        instanceSupplier = { context.serviceContainer.getFunctionService(boundFunction) },
        paramClass = paramClass
    )
}

internal inline fun <reified T : Any> ClassPathFunction.toMemberParamFunction() =
    MemberParamFunction(function, instanceSupplier = { instance }, T::class)

internal inline fun <reified T : Any, R> KFunction<R>.toMemberParamFunction(context: BContext) =
    MemberParamFunction(context, this, T::class)

internal fun <T : Any, R> KFunction<R>.toMemberParamFunction(context: BContext, paramType: KClass<T>) =
    MemberParamFunction(context, this, paramType)

internal inline fun <reified T : Any, R> IBuilderFunctionHolder<R>.toMemberParamFunction(context: BContext): MemberParamFunction<T, R> {
    if (this is ExecutableCommandBuilderImpl<*, *>) {
        requireAt(function.nonEventParameters.size == optionAggregateBuilders.size, function) {
            "Function must have the same number of options declared as on the method"
        }
    } else if (this is TextCommandVariationBuilderImpl) {
        requireAt(function.nonEventParameters.size == optionAggregateBuilders.size, function) {
            "Function must have the same number of options declared as on the method"
        }
    }

    return MemberParamFunction(context, this.function, T::class)
}