package io.github.freya022.botcommands.internal.commands.text.options.builder

import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.options.TextCommandParameter
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import io.github.freya022.botcommands.internal.commands.text.options.TextGeneratedOption
import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class TextGeneratedOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
    val generatedValueSupplier: TextGeneratedValueSupplier
) : AbstractGeneratedOptionBuilderImpl(optionParameter) {
    override fun toGeneratedOption(parent: AggregatedParameter) =
        TextGeneratedOption(parent as TextCommandParameter, this)
}