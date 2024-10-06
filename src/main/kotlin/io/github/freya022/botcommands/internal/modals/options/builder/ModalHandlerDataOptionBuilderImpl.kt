package io.github.freya022.botcommands.internal.modals.options.builder

import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.modals.ModalHandlerInfo
import io.github.freya022.botcommands.internal.modals.options.ModalHandlerDataOption
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class ModalHandlerDataOptionBuilderImpl(
    optionParameter: OptionParameter
) : AbstractGeneratedOptionBuilderImpl(optionParameter) {
    override fun toGeneratedOption(executable: Executable) =
        ModalHandlerDataOption(executable as ModalHandlerInfo, this)
}
