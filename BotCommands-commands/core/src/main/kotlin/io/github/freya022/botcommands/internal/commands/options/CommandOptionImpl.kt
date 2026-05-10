package io.github.freya022.botcommands.internal.commands.options

import io.github.freya022.botcommands.api.commands.options.CommandOption
import io.github.freya022.botcommands.internal.commands.options.builder.CommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

abstract class CommandOptionImpl(
    commandOptionBuilder: CommandOptionBuilderImpl
) : OptionImpl(commandOptionBuilder.optionParameter, OptionType.OPTION, commandOptionBuilder.isOptional),
    CommandOption
