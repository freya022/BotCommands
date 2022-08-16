package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.internal.GeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import kotlin.reflect.KParameter

class ApplicationGeneratedMethodParameter(
    override val kParameter: KParameter,
    applicationGeneratedOptionBuilder: ApplicationGeneratedOptionBuilder
) : GeneratedMethodParameter {
    override val methodParameterType = MethodParameterType.GENERATED

    val generatedValueSupplier = applicationGeneratedOptionBuilder.generatedValueSupplier
}
