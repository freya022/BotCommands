package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.api.prefixed.builder.TextOptionBuilder
import java.util.*

class TextParameterData(optionBuilder: TextOptionBuilder) {
    private val optionalName: String
    private val optionalExample: String?

    init {
        optionalName = optionBuilder.optionName
        optionalExample = optionBuilder.example
    }

    fun getOptionalName(): Optional<String> {
        return Optional.ofNullable(optionalName)
    }

    fun getOptionalExample(): Optional<String> {
        return Optional.ofNullable(optionalExample)
    }
}