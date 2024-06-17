package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.internal.utils.throwArgument

enum class ComponentType(val key: Int) {
    GROUP(0),
    BUTTON(1),
    SELECT_MENU(2);

    companion object {
        fun fromId(key: Int): ComponentType {
            return entries.find { it.key == key } ?: throwArgument("Unknown ComponentType: $key")
        }
    }
}