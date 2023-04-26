package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier

abstract class ApplicationCommandOptionAggregateBuilder(
    declaredName: String
) : OptionAggregateBuilder(declaredName) {
    abstract fun customOption(declaredName: String)

    abstract fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier)
}