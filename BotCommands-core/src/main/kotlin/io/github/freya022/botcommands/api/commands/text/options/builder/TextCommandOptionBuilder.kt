package io.github.freya022.botcommands.api.commands.text.options.builder

import io.github.freya022.botcommands.api.commands.options.builder.CommandOptionBuilder

interface TextCommandOptionBuilder : CommandOptionBuilder {
    var helpExample: String?
    var isId: Boolean
}
