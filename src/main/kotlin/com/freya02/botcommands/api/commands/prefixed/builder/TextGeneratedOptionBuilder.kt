package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedOption
import com.freya02.botcommands.internal.commands.prefixed.TextGeneratedOption
import com.freya02.botcommands.internal.parameters.OptionParameter

class TextGeneratedOptionBuilder(
    optionParameter: OptionParameter,
    val generatedValueSupplier: TextGeneratedValueSupplier
) : OptionBuilder(optionParameter), GeneratedOptionBuilder {
    override fun toGeneratedOption(): GeneratedOption =
        TextGeneratedOption(this)
}
