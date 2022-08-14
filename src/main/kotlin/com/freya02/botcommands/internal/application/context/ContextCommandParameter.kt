package com.freya02.botcommands.internal.application.context

import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import kotlin.reflect.KParameter

abstract class ContextCommandParameter<R>(
    parameter: KParameter,
    val resolver: R
) : MethodParameter {
    override val methodParameterType = MethodParameterType.OPTION
    override val kParameter = parameter
}