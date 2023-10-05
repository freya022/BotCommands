package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.internal.utils.throwUser

enum class LifetimeType(val key: Int) {
    PERSISTENT(0),
    EPHEMERAL(1);

    companion object {
        fun fromId(key: Int): LifetimeType {
            return entries.find { it.key == key } ?: throwUser("Unknown LifetimeType: $key")
        }
    }
}
