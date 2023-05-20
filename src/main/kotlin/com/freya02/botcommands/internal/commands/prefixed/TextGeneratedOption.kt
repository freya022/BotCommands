package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextGeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedOption
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

class TextGeneratedOption(
    generatedOptionBuilder: TextGeneratedOptionBuilder
) : OptionImpl(generatedOptionBuilder.optionParameter, OptionType.GENERATED), GeneratedOption {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
