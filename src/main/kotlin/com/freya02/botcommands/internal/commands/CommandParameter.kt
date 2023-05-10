package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.parameters.MethodParameterImpl

abstract class CommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: OptionAggregateBuilder
) : MethodParameterImpl(optionAggregateBuilder.parameter), IAggregatedParameter {
    final override val aggregator = optionAggregateBuilder.aggregator
    final override val aggregatorInstance = context.serviceContainer.getFunctionService(aggregator)
}