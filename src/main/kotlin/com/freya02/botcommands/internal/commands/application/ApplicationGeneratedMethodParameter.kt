package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

class ApplicationGeneratedMethodParameter(
    override val kParameter: KParameter,
    applicationGeneratedOptionBuilder: ApplicationGeneratedOptionBuilder
) : GeneratedMethodParameter {
    override val methodParameterType = MethodParameterType.GENERATED

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    val generatedValueSupplier = applicationGeneratedOptionBuilder.generatedValueSupplier
}
