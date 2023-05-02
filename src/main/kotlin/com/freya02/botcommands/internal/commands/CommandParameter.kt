package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.parameters.MethodParameterImpl

abstract class CommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: OptionAggregateBuilder
) : MethodParameterImpl(optionAggregateBuilder.parameter) {
    val aggregator = optionAggregateBuilder.aggregator
    val aggregatorInstance = context.serviceContainer.getFunctionService(optionAggregateBuilder.aggregator)

    //TODO rename to "options", not all options are command inputs
    abstract val commandOptions: List<Option>
}