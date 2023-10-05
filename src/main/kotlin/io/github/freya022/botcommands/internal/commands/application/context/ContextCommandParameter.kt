package io.github.freya022.botcommands.internal.commands.application.context

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterImpl

abstract class ContextCommandParameter(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : MethodParameterImpl(optionAggregateBuilder.parameter), IAggregatedParameter {
    final override val aggregator = optionAggregateBuilder.aggregator.toAggregatorFunction(context)
}