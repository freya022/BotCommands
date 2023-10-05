package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import io.github.freya022.botcommands.internal.commands.GeneratedOption
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

class ApplicationGeneratedOption(
    generatedOptionBuilder: ApplicationGeneratedOptionBuilder
) : OptionImpl(generatedOptionBuilder.optionParameter, OptionType.GENERATED), GeneratedOption {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
