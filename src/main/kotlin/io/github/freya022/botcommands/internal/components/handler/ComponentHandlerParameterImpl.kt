package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.transform
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KClass

internal class ComponentHandlerParameterImpl internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder<*>,
    eventType: KClass<out GenericComponentInteractionCreateEvent>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, eventType)

    override val nestedAggregatedParameters = aggregateBuilder.nestedAggregates.transform {
        ComponentHandlerParameterImpl(context, it, eventType)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        optionFinalizer = ::ComponentHandlerOption
    )
}