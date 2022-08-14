package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import kotlin.reflect.KParameter

class ComponentHandlerParameter(
    override val kParameter: KParameter,
    val resolver: ComponentParameterResolver
) : MethodParameter {
    override val methodParameterType = MethodParameterType.OPTION
}