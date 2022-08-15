package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.api.prefixed.builder.TextOptionBuilder

class TextParameterData(optionBuilder: TextOptionBuilder) {
    val helpName: String = optionBuilder.optionName
    val helpExample: String? = optionBuilder.helpExample
}