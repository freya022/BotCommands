package io.github.freya022.botcommands.internal.modals.options

import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.modals.ModalHandlerInfo
import io.github.freya022.botcommands.internal.modals.options.builder.ModalHandlerInputOptionBuilderImpl
import io.github.freya022.botcommands.internal.options.CommandOptions
import io.github.freya022.botcommands.internal.options.transform
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin

internal class ModalHandlerParameterImpl internal constructor(
    context: BContextImpl,
    executable: ModalHandlerInfo,
    aggregateBuilder: OptionAggregateBuilderImpl<*>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, ModalEvent::class)

    override val nestedAggregatedParameters = aggregateBuilder.optionAggregateBuilders.transform {
        ModalHandlerParameterImpl(context, executable, it as OptionAggregateBuilderImpl<*>)
    }

    override val options = CommandOptions.transform<ModalHandlerInputOptionBuilderImpl, ModalParameterResolver<*, *>>(
        context,
        executable,
        null,
        aggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> ModalHandlerInputOption(executable, optionBuilder, resolver) }
    )
}