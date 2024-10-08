package io.github.freya022.botcommands.internal.commands.text.options

import io.github.freya022.botcommands.api.commands.text.options.TextCommandParameter
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption

internal class TextGeneratedOption internal constructor(
    override val parent: TextCommandParameter,
    generatedOptionBuilder: TextGeneratedOptionBuilderImpl
) : AbstractGeneratedOption(generatedOptionBuilder.optionParameter) {

    override val executable get() = parent.executable

    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
