package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextOptionBuilder

class TextParameterData(optionBuilder: TextOptionBuilder) {
    val helpName: String = optionBuilder.optionName
    val helpExample: String? = optionBuilder.helpExample
}