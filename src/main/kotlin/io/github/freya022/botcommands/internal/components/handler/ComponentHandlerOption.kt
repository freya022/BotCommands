package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

class ComponentHandlerOption(
    optionBuilder: ComponentHandlerOptionBuilder,
    val resolver: ComponentParameterResolver<*, *>
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)