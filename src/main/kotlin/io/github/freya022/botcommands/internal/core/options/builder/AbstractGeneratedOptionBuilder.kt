package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal abstract class AbstractGeneratedOptionBuilder(
    optionParameter: OptionParameter
) : OptionBuilder(optionParameter) {
    internal abstract fun toGeneratedOption(): AbstractGeneratedOption
}