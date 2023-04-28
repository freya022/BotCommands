package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.CommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.internal.parameters.MultiParameter
import kotlin.reflect.KFunction

abstract class ApplicationCommandOptionAggregateBuilder(
    multiParameter: MultiParameter,
    aggregator: KFunction<*>
) : CommandOptionAggregateBuilder(multiParameter, aggregator) {
    abstract fun customOption(declaredName: String)

    abstract fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier)
}