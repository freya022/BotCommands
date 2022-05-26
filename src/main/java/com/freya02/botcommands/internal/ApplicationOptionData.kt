package com.freya02.botcommands.internal

import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class ApplicationOptionData(parameter: KParameter) {
    val effectiveName: String
    val effectiveDescription: String
    val autocompletionHandlerName: String?

    init {
        val option: AppOption = parameter.findAnnotation() ?: throwInternal("Tried to construct ApplicationOptionData but annotation wasn't found")
        effectiveName = when {
            option.name.isBlank() -> getOptionName(parameter)
            else -> option.name
        }

        effectiveDescription = when {
            option.description.isBlank() -> "No description"
            else -> option.description
        }

        autocompletionHandlerName = when {
            option.autocomplete.isBlank() -> null
            else -> option.autocomplete
        }
    }

    fun hasAutocompletion(): Boolean {
        return autocompletionHandlerName != null
    }

    companion object {
        private fun getOptionName(parameter: KParameter): String { //TODO take
            val name = parameter.name ?: throwUser("Parameter name cannot be deduced as the option's name is not specified on: $parameter")

            val optionNameBuilder = StringBuilder(name.length + 10)
            for (c in name) {
                if (Character.isUpperCase(c)) {
                    optionNameBuilder.append('_').append(c.lowercaseChar())
                } else {
                    optionNameBuilder.append(c)
                }
            }

            return optionNameBuilder.toString()
        }
    }
}