package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.transform

internal class ModalHandlerParameterImpl internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilderImpl<*>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, ModalEvent::class)

    override val nestedAggregatedParameters = aggregateBuilder.optionAggregateBuilders.transform {
        ModalHandlerParameterImpl(context, it as OptionAggregateBuilderImpl<*>)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        optionFinalizer = ::ModalHandlerInputOption
    )
}