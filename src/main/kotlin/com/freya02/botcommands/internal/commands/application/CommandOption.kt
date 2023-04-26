package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.parameters.MethodParameterType

abstract class CommandOption internal constructor(
    optionBuilder: OptionBuilder
) : AbstractOption {
    final override val methodParameterType = MethodParameterType.OPTION

    abstract val resolver: Any
    val discordName = optionBuilder.optionName
}