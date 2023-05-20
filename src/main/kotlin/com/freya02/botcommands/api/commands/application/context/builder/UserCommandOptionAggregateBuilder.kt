package com.freya02.botcommands.api.commands.application.context.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

class UserCommandOptionAggregateBuilder(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder<UserCommandOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    fun option(declaredName: String) {
        this += UserCommandOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun customOption(declaredName: String) {
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        UserCommandOptionAggregateBuilder(aggregatorParameter, aggregator)
}