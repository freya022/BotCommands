package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.CommandOption

interface TextCommandOption : CommandOption, HelpExampleSupplier {
    val helpName: String
    val helpExample: String?
    val isId: Boolean

    val isQuotable: Boolean
}