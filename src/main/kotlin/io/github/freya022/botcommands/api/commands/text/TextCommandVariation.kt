package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.Executable
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.internal.commands.text.TextCommandParameter

interface TextCommandVariation : Executable, IDeclarationSiteHolder {
    override val parameters: List<TextCommandParameter>

    val description: String?
    val usage: String?
    val example: String?

    val completePattern: Regex?
}