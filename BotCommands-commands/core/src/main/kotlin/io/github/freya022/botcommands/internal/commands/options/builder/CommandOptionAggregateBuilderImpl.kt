package io.github.freya022.botcommands.internal.commands.options.builder

import io.github.freya022.botcommands.api.commands.options.builder.CommandOptionAggregateBuilder
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

abstract class CommandOptionAggregateBuilderImpl<T : CommandOptionAggregateBuilder<T>>(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : OptionAggregateBuilderImpl<T>(aggregatorParameter, aggregator),
    CommandOptionAggregateBuilder<T>
