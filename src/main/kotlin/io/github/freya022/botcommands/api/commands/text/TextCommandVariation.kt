package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.Executable
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder

interface TextCommandVariation : Executable, IDeclarationSiteHolder {
    override val parameters: List<TextCommandParameter>

    val allDiscordOptions: List<TextCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<TextCommandOption>()

    val hasMultipleQuotable: Boolean

    val description: String?
    val usage: String?
    val example: String?

    val completePattern: Regex?
}