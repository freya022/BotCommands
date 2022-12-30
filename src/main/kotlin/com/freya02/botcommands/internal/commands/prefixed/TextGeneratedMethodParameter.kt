package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextGeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

class TextGeneratedMethodParameter(
    override val kParameter: KParameter,
    generatedOptionBuilder: TextGeneratedOptionBuilder
) : GeneratedMethodParameter {
    override val methodParameterType = MethodParameterType.GENERATED

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}