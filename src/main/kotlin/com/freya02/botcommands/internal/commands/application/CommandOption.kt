package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.AbstractOptionImpl
import com.freya02.botcommands.internal.parameters.MethodParameterType

abstract class CommandOption internal constructor(
    commandOptionBuilder: CommandOptionBuilder
) : AbstractOptionImpl(commandOptionBuilder.optionParameter, MethodParameterType.OPTION) {
    abstract val resolver: Any
    val discordName = commandOptionBuilder.optionName
}