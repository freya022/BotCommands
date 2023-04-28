package com.freya02.botcommands.internal.commands.application.context

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

abstract class ContextCommandOption(
    optionBuilder: ApplicationCommandOptionBuilder
) : AbstractOption {
    override val methodParameterType = MethodParameterType.OPTION
    override val multiParameter = optionBuilder.multiParameter
    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    abstract val resolver: Any
}