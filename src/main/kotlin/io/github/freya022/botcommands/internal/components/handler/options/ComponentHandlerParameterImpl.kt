package io.github.freya022.botcommands.internal.components.handler.options

import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.options.CommandOptions
import io.github.freya022.botcommands.internal.options.transform
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KClass

internal class ComponentHandlerParameterImpl internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilderImpl<*>,
    eventType: KClass<out GenericComponentInteractionCreateEvent>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, eventType)

    override val nestedAggregatedParameters = aggregateBuilder.optionAggregateBuilders.transform {
        ComponentHandlerParameterImpl(context, it as OptionAggregateBuilderImpl<*>, eventType)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        optionFinalizer = ::ComponentHandlerOption
    )
}