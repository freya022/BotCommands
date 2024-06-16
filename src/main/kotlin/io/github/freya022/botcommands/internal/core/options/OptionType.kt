package io.github.freya022.botcommands.internal.core.options

import io.github.freya022.botcommands.api.core.utils.simpleNestedName

internal enum class OptionType {
    OPTION,
    CUSTOM,
    CONSTANT, //TODO
    SERVICE,
    GENERATED;

    override fun toString(): String {
        return "${javaClass.simpleNestedName}#$name"
    }
}