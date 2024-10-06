package io.github.freya022.botcommands.internal.commands.text.options

import io.github.freya022.botcommands.internal.commands.text.TextCommandVariationImpl
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption

internal class TextGeneratedOption internal constructor(
    override val executable: TextCommandVariationImpl,
    generatedOptionBuilder: TextGeneratedOptionBuilderImpl
) : AbstractGeneratedOption(generatedOptionBuilder.optionParameter) {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
