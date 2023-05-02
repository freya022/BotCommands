package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

class ComponentHandlerOption(
    optionBuilder: ComponentHandlerOptionBuilder,
    val resolver: ComponentParameterResolver<*, *>
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)