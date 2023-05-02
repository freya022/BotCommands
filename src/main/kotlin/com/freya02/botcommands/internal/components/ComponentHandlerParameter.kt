package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.parameters.MethodParameterImpl

class ComponentHandlerParameter(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder
) : MethodParameterImpl(aggregateBuilder.parameter) {
    val aggregator = aggregateBuilder.aggregator
    val aggregatorInstance = context.serviceContainer.getFunctionService(aggregator)

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