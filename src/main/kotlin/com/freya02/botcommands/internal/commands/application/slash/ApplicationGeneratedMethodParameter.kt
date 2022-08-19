package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import kotlin.reflect.KParameter

class ApplicationGeneratedMethodParameter(
    override val kParameter: KParameter,
    applicationGeneratedOptionBuilder: ApplicationGeneratedOptionBuilder
) : GeneratedMethodParameter {
    override val methodParameterType = MethodParameterType.GENERATED

    val generatedValueSupplier = applicationGeneratedOptionBuilder.generatedValueSupplier
}
