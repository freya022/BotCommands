package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.CommandOptionBuilder
import io.github.freya022.botcommands.internal.parameters.OptionParameter

class TextCommandOptionBuilder internal constructor(optionParameter: OptionParameter, val optionName: String) : CommandOptionBuilder(optionParameter) {
    var helpExample: String? = null
    var isId: Boolean = false
}
