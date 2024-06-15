package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toEventAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.transform

internal class ModalHandlerParameterImpl internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder<*>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toEventAggregatorFunction(context)

    override val nestedAggregatedParameters = aggregateBuilder.nestedAggregates.transform {
        ModalHandlerParameterImpl(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        optionFinalizer = ::ModalHandlerInputOption
    )
}