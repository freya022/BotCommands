package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toEventAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class ModalHandlerParameter internal constructor(
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
        optionFinalizer = ::ModalHandlerInputOption
    )
}