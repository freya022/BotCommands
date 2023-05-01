package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.parameters.OptionParameter

class TextCommandOptionBuilder internal constructor(optionParameter: OptionParameter, optionName: String) : CommandOptionBuilder(optionParameter, optionName) {
    var helpExample: String? = null
    var isId: Boolean = false
}
