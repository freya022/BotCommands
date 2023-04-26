package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.CommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import kotlin.reflect.KFunction

abstract class ApplicationCommandOptionAggregateBuilder(
    owner: KFunction<*>,
    declaredName: String
) : CommandOptionAggregateBuilder(owner, declaredName) {
    abstract fun customOption(declaredName: String)

    abstract fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier)
}