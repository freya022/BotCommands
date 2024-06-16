package io.github.freya022.botcommands.internal.commands.text.builder

import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.internal.commands.text.TextGeneratedOption
import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilder
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class TextGeneratedOptionBuilder internal constructor(
    optionParameter: OptionParameter,
    val generatedValueSupplier: TextGeneratedValueSupplier
) : AbstractGeneratedOptionBuilder(optionParameter) {
    override fun toGeneratedOption() = TextGeneratedOption(this)
}