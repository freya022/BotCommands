package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.internal.commands.text.builder.TextGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption

internal class TextGeneratedOption internal constructor(
    generatedOptionBuilder: TextGeneratedOptionBuilderImpl
) : AbstractGeneratedOption(generatedOptionBuilder.optionParameter) {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
