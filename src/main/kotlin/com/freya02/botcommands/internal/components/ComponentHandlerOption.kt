package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

class ComponentHandlerOption(
    optionBuilder: ComponentHandlerOptionBuilder,
    val resolver: ComponentParameterResolver<*, *>
) : AbstractOption {
    override val optionParameter = optionBuilder.optionParameter
    override val methodParameterType = MethodParameterType.OPTION

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
}