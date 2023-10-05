package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.builder.GeneratedOptionBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.commands.GeneratedOption
import io.github.freya022.botcommands.internal.commands.application.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.parameters.OptionParameter

class ApplicationGeneratedOptionBuilder internal constructor(
    optionParameter: OptionParameter,
    val generatedValueSupplier: ApplicationGeneratedValueSupplier
) : OptionBuilder(optionParameter), GeneratedOptionBuilder {
    override fun toGeneratedOption(): GeneratedOption =
        ApplicationGeneratedOption(this)
}
