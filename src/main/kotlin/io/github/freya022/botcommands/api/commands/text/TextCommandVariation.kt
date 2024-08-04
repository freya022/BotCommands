package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.ICommandOptionContainer
import io.github.freya022.botcommands.api.commands.ICommandParameterContainer
import io.github.freya022.botcommands.api.commands.IFilterContainer
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.options.TextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolder

/**
 * Represents a text command variation, see [TextCommandBuilder.variation] for more details.
 */
interface TextCommandVariation : Executable, IDeclarationSiteHolder,
                                 ICommandParameterContainer, ICommandOptionContainer,
                                 IFilterContainer {
    /**
     * The main context.
     */
    val context: BContext

    /**
     * The text command this variation is from,
     * a text command has one or more variations,
     * see [TextCommandBuilder.variation] for details.
     */
    val command: TextCommandInfo

    override val parameters: List<TextCommandParameter>

    override val discordOptions: List<TextCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<TextCommandOption>()

    /**
     * Whether this text command variation has multiple options which are [quotable][TextCommandOption.isQuotable]
     */
    val hasMultipleQuotable: Boolean
        get() = discordOptions.count { o -> o.isQuotable } > 1

    /**
     * Description of this text command variation.
     *
     * This is shown above each variation's usage/example in the built-in help content.
     */
    val description: String?

    /**
     * Custom usage syntax for this text command variation.
     *
     * If `null`, one will be created out of the [discord options][discordOptions].
     */
    val usage: String?

    /**
     * Custom example for this text command variation.
     *
     * If `null`, one will be created out of the [discord options][discordOptions].
     */
    val example: String?

    /**
     * The regex pattern which matches this text command variation with its options.
     *
     * This is `null` on fallback command variations (those that use [CommandEvent]).
     */
    val completePattern: Regex?

    override fun getParameter(declaredName: String): TextCommandParameter? =
        parameters.find { it.name == declaredName }

    /**
     * Returns the option with the supplied *display name* (i.e., the name you see on the built-in help command),
     * or `null` if not found.
     */
    fun getOptionByDisplayName(name: String): TextCommandOption? =
        discordOptions.find { it.helpName == name }
}