package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.ModalParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.parameters.MethodParameterImpl

class ModalHandlerParameter(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder
) : MethodParameterImpl(aggregateBuilder.parameter), IAggregatedParameter {
    override val aggregator = aggregateBuilder.aggregator
    override val aggregatorInstance: Any? = context.serviceContainer.getFunctionServiceOrNull(aggregator)

    override val commandOptions = CommandOptions.transform(
        context,
        aggregateBuilder,
        object : CommandOptions.Configuration<ModalHandlerInputOptionBuilder, ModalParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: ModalHandlerInputOptionBuilder,
                resolver: ModalParameterResolver<*, *>
            ) = ModalHandlerInputOption(optionBuilder, resolver)
        }
    )
}