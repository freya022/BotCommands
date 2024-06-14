package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.CommandOption
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver

interface TextCommandOption : CommandOption {
    val helpName: String
    val helpExample: String?
    val isId: Boolean

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