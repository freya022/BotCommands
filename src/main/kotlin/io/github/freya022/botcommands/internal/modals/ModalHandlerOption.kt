package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

internal abstract class ModalHandlerOption(
    optionBuilder: OptionBuilder
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)