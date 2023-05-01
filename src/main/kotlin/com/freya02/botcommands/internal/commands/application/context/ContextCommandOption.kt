package com.freya02.botcommands.internal.commands.application.context

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import com.freya02.botcommands.internal.AbstractOptionImpl
import com.freya02.botcommands.internal.parameters.MethodParameterType

abstract class ContextCommandOption(
    optionBuilder: ApplicationCommandOptionBuilder
) : AbstractOptionImpl(optionBuilder.optionParameter, MethodParameterType.OPTION) {
    abstract val resolver: Any
}