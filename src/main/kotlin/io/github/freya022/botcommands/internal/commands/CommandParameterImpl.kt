package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.core.reflection.toEventAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin

internal abstract class CommandParameterImpl internal constructor(
    context: BContext,
    optionAggregateBuilder: OptionAggregateBuilder<*>
) : AbstractMethodParameter(optionAggregateBuilder.parameter), CommandParameter, AggregatedParameterMixin {
    final override val aggregator = optionAggregateBuilder.aggregator.toEventAggregatorFunction(context)
}