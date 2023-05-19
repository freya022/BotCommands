package com.freya02.botcommands.internal.commands.application.context

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.parameters.MethodParameterImpl

abstract class ContextCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : MethodParameterImpl(optionAggregateBuilder.parameter), IAggregatedParameter {
    final override val aggregator = optionAggregateBuilder.aggregator
    final override val aggregatorInstance: Any? = context.serviceContainer.getFunctionServiceOrNull(aggregator)
}