package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.throwUser

enum class LifetimeType(val key: Int) {
    PERSISTENT(1),
    EPHEMERAL(2);

    companion object {
        fun fromId(key: Int): LifetimeType {
            return values().find { it.key == key } ?: throwUser("Unknown LifetimeType: $key")
        }
    }
}
