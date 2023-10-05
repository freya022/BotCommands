package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.commands.builder.GeneratedOptionBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class ModalHandlerDataOptionBuilder(optionParameter: OptionParameter) : OptionBuilder(optionParameter), GeneratedOptionBuilder {
    override fun toGeneratedOption() = ModalHandlerDataOption(this)
}
