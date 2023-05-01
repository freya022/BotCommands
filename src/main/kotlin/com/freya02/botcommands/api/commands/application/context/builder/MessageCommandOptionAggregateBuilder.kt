package com.freya02.botcommands.api.commands.application.context.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.parameters.MultiParameter
import kotlin.reflect.KFunction

class MessageCommandOptionAggregateBuilder(
    multiParameter: MultiParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder(multiParameter, aggregator) {
    fun option(declaredName: String) {
        this += MessageCommandOptionBuilder(multiParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun customOption(declaredName: String) {
        this += CustomOptionBuilder(multiParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(multiParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }
}