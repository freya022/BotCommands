package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.builder.GeneratedOptionBuilder
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import kotlin.reflect.KParameter

class GeneratedMethodParameter(override val kParameter: KParameter, val generatedOptionBuilder: GeneratedOptionBuilder) : MethodParameter {
    override val methodParameterType = MethodParameterType.GENERATED
}
