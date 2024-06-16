package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.ICommandOptionContainer
import io.github.freya022.botcommands.api.commands.IFilterContainer
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Executable

interface TextCommandVariation : Executable, IDeclarationSiteHolder, ICommandOptionContainer, IFilterContainer {
    val context: BContext

    override val parameters: List<TextCommandParameter>

    override val discordOptions: List<TextCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<TextCommandOption>()

    val hasMultipleQuotable: Boolean
        get() = discordOptions.count { o -> o.isQuotable } > 1

    val description: String?
    val usage: String?
    val example: String?

    val completePattern: Regex?
}