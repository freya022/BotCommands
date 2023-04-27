package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.ModalParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

open class ModalHandlerParameter(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder
) : MethodParameter {
    override val kParameter = aggregateBuilder.parameter
    override val methodParameterType = MethodParameterType.OPTION
    val aggregator = aggregateBuilder.aggregator
    val aggregatorInstance = context.serviceContainer.getFunctionService(aggregator)

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    val options = CommandOptions.transform(
        context,
        aggregateBuilder,
        object : CommandOptions.Configuration<ModalHandlerInputOptionBuilder, ModalParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: ModalHandlerInputOptionBuilder,
                resolver: ModalParameterResolver<*, *>
            ) = ModalHandlerInputOption(optionBuilder, resolver)
        }
    )
}