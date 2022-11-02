package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextOptionBuilder
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.commands.CommandParameter
import kotlin.reflect.KParameter

class TextCommandParameter(
    parameter: KParameter,
    optionBuilder: TextOptionBuilder,
    val resolver: RegexParameterResolver<*, *>
) : CommandParameter(parameter, optionBuilder) {
    val groupCount = resolver.preferredPattern.matcher("").groupCount()
    val data = TextParameterData(optionBuilder)
    val isId = optionBuilder.isId
}