package io.github.freya022.botcommands.internal.core.options

import io.github.freya022.botcommands.api.core.utils.simpleNestedName

enum class OptionType {
    OPTION,
    CUSTOM,
    SERVICE,
    GENERATED;

    override fun toString(): String {
        return "${javaClass.simpleNestedName}#$name"
    }
}
