package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.core.BContextImpl
import com.freya02.botcommands.internal.core.reflection.toAggregatorFunction
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.parameters.MethodParameterImpl
import com.freya02.botcommands.internal.transform

class ComponentHandlerParameter(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder<*>
) : MethodParameterImpl(aggregateBuilder.parameter), IAggregatedParameter {
    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context)

    override val nestedAggregatedParameters = aggregateBuilder.nestedAggregates.transform {
        ComponentHandlerParameter(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        aggregateBuilder,
        object : CommandOptions.Configuration<ComponentHandlerOptionBuilder, ComponentParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: ComponentHandlerOptionBuilder,
                resolver: ComponentParameterResolver<*, *>
            ) = ComponentHandlerOption(optionBuilder, resolver)
        })
}