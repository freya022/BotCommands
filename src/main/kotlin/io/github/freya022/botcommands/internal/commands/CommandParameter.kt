package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterImpl

abstract class CommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: OptionAggregateBuilder<*>
) : MethodParameterImpl(optionAggregateBuilder.parameter), IAggregatedParameter {
    final override val aggregator = optionAggregateBuilder.aggregator.toAggregatorFunction(context)
}