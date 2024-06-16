package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilder
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class ModalHandlerDataOptionBuilder(
    optionParameter: OptionParameter
) : AbstractGeneratedOptionBuilder(optionParameter) {
    override fun toGeneratedOption() = ModalHandlerDataOption(this)
}
