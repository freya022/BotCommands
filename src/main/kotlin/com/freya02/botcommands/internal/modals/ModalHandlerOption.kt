package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

abstract class ModalHandlerOption(
    optionBuilder: OptionBuilder
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)