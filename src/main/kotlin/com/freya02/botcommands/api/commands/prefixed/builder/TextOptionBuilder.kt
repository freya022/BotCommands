package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder

class TextOptionBuilder internal constructor(declaredName: String, optionName: String) : OptionBuilder(declaredName, optionName) {
    var helpExample: String? = null
    var isId: Boolean = false
}
