package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandOptionBuilder
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.commands.CommandParameter

class TextCommandParameter(
    optionBuilder: TextCommandOptionBuilder,
    val resolver: RegexParameterResolver<*, *>
) : CommandParameter(context, optionBuilder) {
    val groupCount = resolver.preferredPattern.matcher("").groupCount()
    val data = TextParameterData(optionBuilder)
    val isId = optionBuilder.isId
}