package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toEventAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterImpl
import io.github.freya022.botcommands.internal.transform

class ModalHandlerParameter internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder<*>
) : MethodParameterImpl(aggregateBuilder.parameter), IAggregatedParameter {
    override val aggregator = aggregateBuilder.aggregator.toEventAggregatorFunction(context)

    override val nestedAggregatedParameters = aggregateBuilder.nestedAggregates.transform {
        ModalHandlerParameter(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        object : CommandOptions.Configuration<ModalHandlerInputOptionBuilder, ModalParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: ModalHandlerInputOptionBuilder,
                resolver: ModalParameterResolver<*, *>
            ) = ModalHandlerInputOption(optionBuilder, resolver)
        }
    )
}