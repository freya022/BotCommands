package io.github.freya022.botcommands.internal.commands.application.options.builder

import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.internal.commands.options.builder.CommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal abstract class ApplicationCommandOptionAggregateBuilderImpl<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>,
) : CommandOptionAggregateBuilderImpl<T>(aggregatorParameter, aggregator),
    ApplicationCommandOptionAggregateBuilder<T>