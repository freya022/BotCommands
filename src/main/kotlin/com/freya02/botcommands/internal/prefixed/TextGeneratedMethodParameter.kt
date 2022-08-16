package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.api.builder.TextGeneratedOptionBuilder
import com.freya02.botcommands.internal.GeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import kotlin.reflect.KParameter

class TextGeneratedMethodParameter(
    override val kParameter: KParameter,
    generatedOptionBuilder: TextGeneratedOptionBuilder
) : GeneratedMethodParameter {
    override val methodParameterType = MethodParameterType.GENERATED

    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
