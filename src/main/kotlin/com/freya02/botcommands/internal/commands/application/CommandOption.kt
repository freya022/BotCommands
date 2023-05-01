package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

abstract class CommandOption internal constructor(
    commandOptionBuilder: CommandOptionBuilder
) : AbstractOption {
    final override val methodParameterType = MethodParameterType.OPTION
    final override val multiParameter = commandOptionBuilder.multiParameter
    final override val kParameter = super.kParameter
    final override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    final override val isVararg = kParameter.isVararg

    abstract val resolver: Any
    val discordName = commandOptionBuilder.optionName
}