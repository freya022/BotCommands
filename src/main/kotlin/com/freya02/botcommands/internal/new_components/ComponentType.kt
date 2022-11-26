package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.internal.throwUser

enum class ComponentType(val key: Int) {
    GROUP(0),
    BUTTON(1),
    SELECT_MENU(2);

    companion object {
        fun fromId(key: Int): ComponentType {
            return ComponentType.values().find { it.key == key } ?: throwUser("Unknown ComponentType: $key")
        }
    }
}