package io.github.freya022.botcommands.internal.modals.options

import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.modals.ModalHandlerInfo
import io.github.freya022.botcommands.internal.options.CommandOptions
import io.github.freya022.botcommands.internal.options.transform
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin

internal class ModalHandlerParameterImpl internal constructor(
    override val executable: ModalHandlerInfo,
    aggregateBuilder: OptionAggregateBuilderImpl<*>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, ModalEvent::class)

    override val nestedAggregatedParameters = aggregateBuilder.optionAggregateBuilders.transform {
        ModalHandlerParameterImpl(executable, it as OptionAggregateBuilderImpl<*>)
    }

    override val options = CommandOptions.transform(
        this,
        null,
        aggregateBuilder,
        optionFinalizer = ::ModalHandlerInputOption
    )
}