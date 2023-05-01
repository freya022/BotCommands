package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.AbstractOptionImpl
import com.freya02.botcommands.internal.parameters.MethodParameterType

class ComponentHandlerOption(
    optionBuilder: ComponentHandlerOptionBuilder,
    val resolver: ComponentParameterResolver<*, *>
) : AbstractOptionImpl(optionBuilder.optionParameter, MethodParameterType.OPTION)