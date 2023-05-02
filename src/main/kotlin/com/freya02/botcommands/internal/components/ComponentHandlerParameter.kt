package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

class ComponentHandlerParameter(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder
) : MethodParameter {
    override val kParameter = aggregateBuilder.parameter
    val aggregator = aggregateBuilder.aggregator
    val aggregatorInstance = context.serviceContainer.getFunctionService(aggregator)

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    val options = CommandOptions.transform(
        context,
        aggregateBuilder,
        object : CommandOptions.Configuration<ComponentHandlerOptionBuilder, ComponentParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: ComponentHandlerOptionBuilder,
                resolver: ComponentParameterResolver<*, *>
            ) = ComponentHandlerOption(optionBuilder, resolver)
        })
}