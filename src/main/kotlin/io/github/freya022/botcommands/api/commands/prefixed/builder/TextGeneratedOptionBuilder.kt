package io.github.freya022.botcommands.api.commands.prefixed.builder

import io.github.freya022.botcommands.api.commands.builder.GeneratedOptionBuilder
import io.github.freya022.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.commands.GeneratedOption
import io.github.freya022.botcommands.internal.commands.prefixed.TextGeneratedOption
import io.github.freya022.botcommands.internal.parameters.OptionParameter

class TextGeneratedOptionBuilder internal constructor(
    optionParameter: OptionParameter,
    val generatedValueSupplier: TextGeneratedValueSupplier
) : OptionBuilder(optionParameter), GeneratedOptionBuilder {
    override fun toGeneratedOption(): GeneratedOption =
        TextGeneratedOption(this)
}
