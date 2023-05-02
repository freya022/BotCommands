package com.freya02.botcommands.internal.commands.application.context

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

abstract class ContextCommandOption(
    optionBuilder: ApplicationCommandOptionBuilder
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION) {
    abstract val resolver: Any
}