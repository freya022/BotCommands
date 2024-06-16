package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.internal.commands.text.builder.TextGeneratedOptionBuilder
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption

internal class TextGeneratedOption internal constructor(
    generatedOptionBuilder: TextGeneratedOptionBuilder
) : AbstractGeneratedOption(generatedOptionBuilder.optionParameter) {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
