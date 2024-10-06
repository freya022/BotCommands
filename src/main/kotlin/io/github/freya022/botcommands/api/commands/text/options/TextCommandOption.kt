package io.github.freya022.botcommands.api.commands.text.options

import io.github.freya022.botcommands.api.commands.options.CommandOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver

/**
 * Represents a Discord input of a text command.
 */
interface TextCommandOption : CommandOption {

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command get() = executable
    override val executable: TextCommandVariation

    /**
     * Name of the text command option,
     * also displayed on the "Usage" part of the built-in help command.
     */
    val helpName: String

    /**
     * Example input for the text command option,
     * displayed on the "Example" part of the built-in help command.
     */
    val helpExample: String?

    /**
     * Whether this option needs to be considered as a snowflake ID.
     *
     * Mainly used by [TextParameterResolver.getHelpExample].
     */
    val isId: Boolean

    /**
     * Whether this option's resolver supports quotes.
     *
     * Quotes are used when multiple quotable options are used, such as two strings,
     * which helps to parse command arguments.
     */
    val isQuotable: Boolean

    /**
     * Returns a help example for this option,
     * from this option's [TextParameterResolver][TextParameterResolver.getHelpExample].
     *
     * The difference with [helpExample] is that the example comes from the resolver.
     * Think of a context-specific example ([helpExample]) vs. a generic help example (resolver).
     *
     * **Tip:** You may use the event as a way to get sample data (such as getting the member, channel, roles, etc...).
     *
     * @param event     The event of the command that triggered help content to be displayed
     */
    fun getResolverHelpExample(event: BaseCommandEvent): String
}