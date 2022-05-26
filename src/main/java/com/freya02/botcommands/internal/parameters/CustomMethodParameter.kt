package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.parameters.CustomResolver
import kotlin.reflect.KParameter

class CustomMethodParameter(override val kParameter: KParameter, val resolver: CustomResolver) : MethodParameter {
    override val methodParameterType = MethodParameterType.CUSTOM

    override val name = kParameter.name!! //TODO is this even needed?
}
