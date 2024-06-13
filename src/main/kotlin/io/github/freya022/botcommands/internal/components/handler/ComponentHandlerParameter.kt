package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toEventAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class ComponentHandlerParameter internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder<*>
) : MethodParameterImpl(aggregateBuilder.parameter), IAggregatedParameter {
    override val aggregator = aggregateBuilder.aggregator.toEventAggregatorFunction(context)

    override val nestedAggregatedParameters = aggregateBuilder.nestedAggregates.transform {
        ComponentHandlerParameter(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        optionFinalizer = ::ComponentHandlerOption
    )
}