package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextGeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.core.options.AbstractOptionImpl
import com.freya02.botcommands.internal.parameters.MethodParameterType

class TextGeneratedMethodParameter(
    generatedOptionBuilder: TextGeneratedOptionBuilder
) : AbstractOptionImpl(generatedOptionBuilder.optionParameter, MethodParameterType.GENERATED), GeneratedMethodParameter {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
