package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.INamedCommand
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.entities.Guild
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
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

internal fun INamedCommand.lazyPath(): Lazy<CommandPath> = lazy {
    val components = mutableListOf<String>()
    var info = this

    do {
        components.add(info.name)
        info = info.parentInstance ?: break
    } while (true)

    CommandPath.of(components)
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

internal fun <K, V> MutableMap<K, V>.putIfAbsentOrThrowInternal(key: K, value: V) {
    if (key in this)
        throwInternal("Key '$key' is already present in the map")
    this[key] = value
}

internal inline fun <K, V> MutableMap<K, V>.putIfAbsentOrThrow(key: K, value: V, messageSupplier: (value: V) -> String) {
    val existingValue = this[key]
    if(existingValue != null) throw IllegalStateException(messageSupplier(existingValue))
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

internal fun Duration.toTimestampIfFinite(): Instant? =
    takeIfFinite()?.let { Clock.System.now() + it }

internal fun Duration.takeIfFinite(): Duration? =
    takeIf { it.isFinite() && it.isPositive() }

internal class WriteOnce<T : Any> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throwState("Property ${property.name} must be initialized before getting it.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        check(this.value == null) {
            "Cannot set value twice"
        }
        this.value = value
    }

    internal fun isInitialized(): Boolean = value != null
}