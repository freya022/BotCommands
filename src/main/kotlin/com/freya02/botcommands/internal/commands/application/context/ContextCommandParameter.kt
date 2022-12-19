package com.freya02.botcommands.internal.commands.application.context

import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

abstract class ContextCommandParameter<R>(
    parameter: KParameter,
    val resolver: R
) : MethodParameter {
    override val methodParameterType = MethodParameterType.OPTION
    override val kParameter = parameter
    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
}