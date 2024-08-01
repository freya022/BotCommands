package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KClass

internal abstract class CommandParameterImpl internal constructor(
    context: BContext,
    optionAggregateBuilder: OptionAggregateBuilderImpl<*>,
    eventType: KClass<out Event>
) : AbstractMethodParameter(optionAggregateBuilder.parameter), CommandParameter, AggregatedParameterMixin {
    final override val aggregator = optionAggregateBuilder.aggregator.toAggregatorFunction(context, eventType)
}