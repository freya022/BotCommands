package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.CommandOption

class TextCommandOption(
    optionBuilder: TextCommandOptionBuilder,
    override val resolver: TextParameterResolver<*, *>
) : CommandOption(optionBuilder) {
    val groupCount = resolver.preferredPattern.matcher("").groupCount()
    val helpName: String = optionBuilder.optionName
    val helpExample: String? = optionBuilder.helpExample
    val isId = optionBuilder.isId
}