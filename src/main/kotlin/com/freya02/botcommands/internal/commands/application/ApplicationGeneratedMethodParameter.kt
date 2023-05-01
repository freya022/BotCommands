package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.internal.AbstractOptionImpl
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType

class ApplicationGeneratedMethodParameter(
    generatedOptionBuilder: ApplicationGeneratedOptionBuilder
) : AbstractOptionImpl(generatedOptionBuilder.optionParameter, MethodParameterType.GENERATED), GeneratedMethodParameter {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
