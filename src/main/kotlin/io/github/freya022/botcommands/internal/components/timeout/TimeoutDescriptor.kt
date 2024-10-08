package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.components.timeout.options.TimeoutHandlerParameter
import io.github.freya022.botcommands.internal.components.timeout.options.builder.TimeoutHandlerOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.options.builder.ServiceOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.core.service.canCreateWrappedService
import io.github.freya022.botcommands.internal.options.transformParameters
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.shortSignature
import kotlin.reflect.KClass

internal class TimeoutDescriptor<T : Any> internal constructor(
    context: BContextImpl,
    override val eventFunction: MemberParamFunction<T, *>,
    aggregatorFirstParamType: KClass<T>
) : ExecutableMixin {
    override val parameters: List<TimeoutHandlerParameter>

    init {
        parameters = eventFunction.transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val optionParameter = OptionParameter.fromSelfAggregate(function, declaredName)
                if (parameter.hasAnnotationRecursive<TimeoutData>()) {
                    TimeoutHandlerOptionBuilderImpl(optionParameter)
                } else {
                    val serviceError = context.serviceContainer.canCreateWrappedService(parameter)
                    require(serviceError == null) {
                        "Could not get service parameter for '$declaredName', in ${function.shortSignature}, did you forget to use ${annotationRef<TimeoutData>()}?\n${serviceError!!.toDetailedString()}"
                    }

                    ServiceOptionBuilderImpl(optionParameter)
                }
            },
            aggregateBlock = { TimeoutHandlerParameter(context, it, aggregatorFirstParamType) }
        )
    }

    internal val optionSize = parameters.sumOf { p -> p.allOptions.count { o -> o.optionType == OptionType.OPTION } }
}