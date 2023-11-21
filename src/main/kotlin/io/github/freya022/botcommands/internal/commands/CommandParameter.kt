package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.core.reflection.toEventAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterImpl

abstract class CommandParameter(
    context: BContext,
    optionAggregateBuilder: OptionAggregateBuilder<*>
) : MethodParameterImpl(optionAggregateBuilder.parameter), IAggregatedParameter {
    final override val aggregator = optionAggregateBuilder.aggregator.toEventAggregatorFunction(context)
}