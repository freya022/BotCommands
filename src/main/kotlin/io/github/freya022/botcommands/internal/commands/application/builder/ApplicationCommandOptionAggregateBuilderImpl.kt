package io.github.freya022.botcommands.internal.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.internal.commands.CommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal abstract class ApplicationCommandOptionAggregateBuilderImpl<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>,
) : CommandOptionAggregateBuilderImpl<T>(aggregatorParameter, aggregator),
    ApplicationCommandOptionAggregateBuilder<T>