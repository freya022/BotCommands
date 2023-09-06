package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.internal.commands.GeneratedOption
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

internal class ModalHandlerDataOption internal constructor(
    optionBuilder: ModalHandlerDataOptionBuilder
) : OptionImpl(optionBuilder.optionParameter, OptionType.GENERATED), GeneratedOption