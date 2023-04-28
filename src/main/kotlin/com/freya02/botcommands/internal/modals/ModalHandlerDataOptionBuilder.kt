package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.parameters.MultiParameter

internal class ModalHandlerDataOptionBuilder(multiParameter: MultiParameter) : OptionBuilder(multiParameter), GeneratedOptionBuilder {
    override fun toGeneratedMethodParameter() = ModalHandlerDataOption(this)
}
