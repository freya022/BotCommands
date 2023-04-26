package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

abstract class CommandOption internal constructor(
    optionBuilder: OptionBuilder
) : AbstractOption {
    final override val methodParameterType = MethodParameterType.OPTION
    final override val kParameter = optionBuilder.parameter
    final override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    abstract val resolver: Any
    val discordName = optionBuilder.optionName
}