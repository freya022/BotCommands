package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.builder.TextGeneratedOptionBuilder
import io.github.freya022.botcommands.internal.commands.GeneratedOption
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

class TextGeneratedOption(
    generatedOptionBuilder: TextGeneratedOptionBuilder
) : OptionImpl(generatedOptionBuilder.optionParameter, OptionType.GENERATED), GeneratedOption {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
