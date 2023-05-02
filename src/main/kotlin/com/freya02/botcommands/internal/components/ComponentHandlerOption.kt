package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.core.options.AbstractOptionImpl
import com.freya02.botcommands.internal.core.options.MethodParameterType

class ComponentHandlerOption(
    optionBuilder: ComponentHandlerOptionBuilder,
    val resolver: ComponentParameterResolver<*, *>
) : AbstractOptionImpl(optionBuilder.optionParameter, MethodParameterType.OPTION)