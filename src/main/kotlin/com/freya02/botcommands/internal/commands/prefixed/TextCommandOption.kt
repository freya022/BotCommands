package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandOptionBuilder
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.commands.application.CommandOption

class TextCommandOption(
    optionBuilder: TextCommandOptionBuilder,
    override val resolver: RegexParameterResolver<*, *>
) : CommandOption(optionBuilder) {
    val groupCount = resolver.preferredPattern.matcher("").groupCount()
    val data = TextParameterData(optionBuilder)
    val isId = optionBuilder.isId
}