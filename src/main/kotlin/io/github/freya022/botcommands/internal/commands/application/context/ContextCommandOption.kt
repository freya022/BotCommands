package io.github.freya022.botcommands.internal.commands.application.context

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

abstract class ContextCommandOption(
    optionBuilder: ApplicationCommandOptionBuilder
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION) {
    abstract val resolver: Any
}