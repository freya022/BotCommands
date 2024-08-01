package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandOptionAggregateBuilder
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal abstract class CommandOptionAggregateBuilderImpl<T : CommandOptionAggregateBuilder<T>> internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : OptionAggregateBuilderImpl<T>(aggregatorParameter, aggregator),
    CommandOptionAggregateBuilder<T>