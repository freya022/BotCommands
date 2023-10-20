package io.github.freya022.botcommands.internal.utils

import net.dv8tion.jda.api.entities.Guild
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal fun String.toDiscordString(): String {
    val sb: StringBuilder = StringBuilder()

    for (c in this) {
        if (c.isUpperCase()) {
            sb.append('_').append(c.lowercaseChar())
        } else {
            sb.append(c)
        }
    }

    return sb.toString()
}

internal fun Guild?.asScopeString() = if (this == null) "global scope" else "guild '${this.name}' (${this.id})"

@OptIn(ExperimentalContracts::class)
internal inline fun <reified T> downcast(obj: Any): T {
    contract {
        returns() implies (obj is T)
    }

    if (obj as? T == null) {
        throwInternal("${obj::class.simpleName} should implement ${T::class.simpleName}")
    }
    return obj
}