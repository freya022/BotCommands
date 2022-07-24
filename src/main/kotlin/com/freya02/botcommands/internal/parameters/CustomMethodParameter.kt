package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.internal.findDeclarationName
import kotlin.reflect.KParameter

class CustomMethodParameter(override val kParameter: KParameter, val resolver: CustomResolver) : MethodParameter {
    override val methodParameterType = MethodParameterType.CUSTOM

    override val name = kParameter.findDeclarationName() //TODO is this even needed?
}