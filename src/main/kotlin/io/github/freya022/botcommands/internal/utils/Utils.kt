package io.github.freya022.botcommands.internal.utils

import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Guild
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.Duration

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

internal fun <K, V> MutableMap<K, V>.putIfAbsentOrThrow(key: K, value: V) {
    if (key in this)
        throwInternal("Key '$key' is already present in the map")
    this[key] = value
}

internal inline fun CoroutineScope.launchCatching(
    crossinline catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
    crossinline block: suspend CoroutineScope.() -> Unit
): Job = launch {
    runCatching(catchBlock, block)
}

internal inline fun CoroutineScope.launchCatchingDelayed(
    delay: Duration,
    crossinline catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
    crossinline block: suspend CoroutineScope.() -> Unit
): Job = launch {
    delay(delay)
    runCatching(catchBlock, block)
}

private suspend inline fun CoroutineScope.runCatching(
    crossinline catchBlock: suspend (CoroutineScope, Throwable) -> Unit,
    crossinline block: suspend (CoroutineScope) -> Unit
) {
    try {
        block(this)
    } catch (e: CancellationException) {
        // Pass cancellation exceptions back,
        // at worst JobSupport#cancelParent makes the exception ignored
        throw e
    } catch (e: Throwable) {
        catchBlock(this, e)
    }
}