package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.throwUser

internal enum class LifetimeType(val id: Int) {
    PERSISTENT(1),
    EPHEMERAL(2);

    companion object {
        fun fromId(id: Int): LifetimeType {
            return values().find { it.id == id } ?: throwUser("Unknown LifetimeType: $id")
        }
    }
}
