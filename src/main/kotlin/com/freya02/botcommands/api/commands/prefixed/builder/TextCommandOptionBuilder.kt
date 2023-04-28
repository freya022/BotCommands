package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.parameters.MultiParameter

class TextCommandOptionBuilder internal constructor(multiParameter: MultiParameter, optionName: String) : CommandOptionBuilder(multiParameter, optionName) {
    var helpExample: String? = null
    var isId: Boolean = false
}
