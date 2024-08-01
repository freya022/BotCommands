package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.CommandOptionBuilder

interface TextCommandOptionBuilder : CommandOptionBuilder {
    var helpExample: String?
    var isId: Boolean
}
