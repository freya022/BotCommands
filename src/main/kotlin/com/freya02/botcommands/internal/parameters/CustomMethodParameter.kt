package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.parameters.ICustomResolver
import kotlin.reflect.KParameter

class CustomMethodParameter(override val kParameter: KParameter, val resolver: ICustomResolver) : MethodParameter {
    override val methodParameterType = MethodParameterType.CUSTOM
}