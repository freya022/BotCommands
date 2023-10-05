package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.internal.commands.GeneratedOption
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

internal class ModalHandlerDataOption internal constructor(
    optionBuilder: ModalHandlerDataOptionBuilder
) : OptionImpl(optionBuilder.optionParameter, OptionType.GENERATED), GeneratedOption