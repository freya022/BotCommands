package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.parameters.OptionParameter

internal class ModalHandlerDataOptionBuilder(optionParameter: OptionParameter) : OptionBuilder(optionParameter), GeneratedOptionBuilder {
    override fun toGeneratedOption() = ModalHandlerDataOption(this)
}
