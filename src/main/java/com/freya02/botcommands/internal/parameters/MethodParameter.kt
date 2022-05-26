package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.isPrimitive
import com.freya02.botcommands.internal.isReallyOptional
import kotlin.reflect.KParameter
import kotlin.reflect.KType

interface MethodParameter {
    val methodParameterType: MethodParameterType
    val kParameter: KParameter
    val name: String

    val type: KType
        get() = kParameter.type
    val index: Int
        get() = kParameter.index
    val isOption: Boolean
        get() = methodParameterType != MethodParameterType.CUSTOM
    val isOptional: Boolean
        get() = kParameter.isReallyOptional
    val isPrimitive: Boolean
        get() = kParameter.isPrimitive
}