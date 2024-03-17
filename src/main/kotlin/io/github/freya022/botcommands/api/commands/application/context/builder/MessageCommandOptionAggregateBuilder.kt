package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

class MessageCommandOptionAggregateBuilder internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder<MessageCommandOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    fun option(declaredName: String) {
        this += MessageCommandOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun customOption(declaredName: String) {
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        MessageCommandOptionAggregateBuilder(aggregatorParameter, aggregator)
}