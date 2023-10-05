package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.CommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

abstract class ApplicationCommandOptionAggregateBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : CommandOptionAggregateBuilder<T>(aggregatorParameter, aggregator) {
    abstract fun customOption(declaredName: String)

    abstract fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier)
}