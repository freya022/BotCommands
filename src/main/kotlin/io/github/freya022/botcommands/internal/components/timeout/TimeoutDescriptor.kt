package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.transformParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

internal class TimeoutDescriptor<T : Any> internal constructor(
    context: BContextImpl,
    override val eventFunction: MemberParamFunction<T, *>,
    aggregatorFirstParamType: KClass<T>
) : IExecutableInteractionInfo {
    override val parameters: List<TimeoutHandlerParameter>

    init {
        parameters = function.nonInstanceParameters.drop(1).transformParameters(
            builderBlock = { function, parameter, declaredName ->
                when (context.serviceContainer.canCreateService(parameter.type.jvmErasure)) {
                    //No error => is a service
                    null -> CustomOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    else -> TimeoutHandlerOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                }
            },
            aggregateBlock = { TimeoutHandlerParameter(context, it, aggregatorFirstParamType) }
        )
    }

    internal val optionSize = parameters.sumOf { p -> p.allOptions.count { o -> o.optionType == OptionType.OPTION } }
}