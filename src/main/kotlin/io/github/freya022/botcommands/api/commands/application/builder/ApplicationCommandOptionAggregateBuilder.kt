package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.CommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

abstract class ApplicationCommandOptionAggregateBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : CommandOptionAggregateBuilder<T>(aggregatorParameter, aggregator) {
    /**
     * Declares a generated option, the supplier gets called on each command execution.
     *
     * @param declaredName Name of the declared parameter in the aggregator
     *
     * @see GeneratedOption @GeneratedOption
     */
    abstract fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier)
}