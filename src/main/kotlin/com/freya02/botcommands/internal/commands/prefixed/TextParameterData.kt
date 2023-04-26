package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandOptionBuilder

class TextParameterData(optionBuilder: TextCommandOptionBuilder) {
    val helpName: String = optionBuilder.optionName
    val helpExample: String? = optionBuilder.helpExample
}