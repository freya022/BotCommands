package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KParameter

class TextCommandParameter(
    parameter: KParameter,
    optionBuilder: OptionBuilder, //TODO TextOptionBuilder
    val resolver: RegexParameterResolver
) : CommandParameter(
    parameter, optionBuilder
) {
    val groupCount = resolver.getPreferredPattern().matcher("").groupCount()
    val data = TextParameterData(parameter)
    val isId = true //TODO fix
}