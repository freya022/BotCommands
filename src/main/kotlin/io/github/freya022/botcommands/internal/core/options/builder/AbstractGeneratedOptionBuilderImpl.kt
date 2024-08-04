package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal abstract class AbstractGeneratedOptionBuilderImpl(
    optionParameter: OptionParameter
) : OptionBuilderImpl(optionParameter) {
    internal abstract fun toGeneratedOption(): AbstractGeneratedOption
}