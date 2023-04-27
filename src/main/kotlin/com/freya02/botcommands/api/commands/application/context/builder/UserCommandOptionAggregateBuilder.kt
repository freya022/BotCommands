package com.freya02.botcommands.api.commands.application.context.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import kotlin.reflect.KFunction

class UserCommandOptionAggregateBuilder(
    owner: KFunction<*>,
    declaredName: String,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder(owner, declaredName, aggregator) {
    fun option(declaredName: String) {
        this += UserCommandOptionBuilder(owner, declaredName)
    }

    override fun customOption(declaredName: String) {
        this += CustomOptionBuilder(owner, declaredName)
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(owner, declaredName, generatedValueSupplier)
    }
}