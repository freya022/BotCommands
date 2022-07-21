package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.annotations.api.prefixed.annotations.TextOption
import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class TextParameterData(parameter: KParameter) {
    private var optionalName: String? = null
    private var optionalExample: String? = null

    init {
        val option = parameter.findAnnotation<TextOption>()!!

        optionalName = option.name.ifBlank { null }
        optionalExample = option.example.ifBlank { null }
    }

    fun getOptionalName(): Optional<String> {
        return Optional.ofNullable(optionalName)
    }

    fun getOptionalExample(): Optional<String> {
        return Optional.ofNullable(optionalExample)
    }
}