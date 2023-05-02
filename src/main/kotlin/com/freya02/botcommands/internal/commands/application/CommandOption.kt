package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

abstract class CommandOption internal constructor(
    commandOptionBuilder: CommandOptionBuilder
) : OptionImpl(commandOptionBuilder.optionParameter, OptionType.OPTION) {
    abstract val resolver: Any
    val discordName = commandOptionBuilder.optionName
}