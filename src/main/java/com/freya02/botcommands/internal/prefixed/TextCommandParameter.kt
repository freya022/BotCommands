package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.annotations.api.prefixed.annotations.TextOption
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class TextCommandParameter(
    resolverType: KClass<RegexParameterResolver>,
    parameter: KParameter,
    index: Int
) : CommandParameter<RegexParameterResolver>(
    resolverType, parameter, index
) {
    override val optionAnnotations: List<KClass<out Annotation>>
        get() = listOf(TextOption::class)

    val groupCount = _resolver?.preferredPattern?.matcher("")?.groupCount() ?: -1
    val data = TextParameterData(parameter)
    val isId = true //TODO fix
}