package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.IFilterContainer
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Executable

interface TextCommandVariation : Executable, IDeclarationSiteHolder, IFilterContainer {
    val context: BContext

    override val parameters: List<TextCommandParameter>

    val allDiscordOptions: List<TextCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<TextCommandOption>()

    val hasMultipleQuotable: Boolean

    val description: String?
    val usage: String?
    val example: String?

    val completePattern: Regex?
}