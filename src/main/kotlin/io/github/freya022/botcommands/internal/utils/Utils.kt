package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.internal.core.exceptions.InitializationException
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

internal inline fun <R> runInitialization(block: () -> R): R {
    try {
        return block()
    } catch (e: Throwable) {
        throw InitializationException("An exception occurred while building the framework", e)
    }
}

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