package io.github.freya022.botcommands.api.commands.text.options.builder

import io.github.freya022.botcommands.api.commands.options.builder.CommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver

interface TextCommandOptionBuilder : CommandOptionBuilder {
    /**
     * Example input for the text command option,
     * displayed on the "Example" part of the built-in help command.
     */
    var helpExample: String?

    /**
     * Whether this option needs to be considered as a snowflake ID.
     *
     * Mainly used by [TextParameterResolver.getHelpExample].
     */
    var isId: Boolean
}
