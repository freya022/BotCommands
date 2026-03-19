package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.INamedCommand
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

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
        components.add(index = 0, info.name)
        info = info.parentInstance ?: break
    } while (true)

    CommandPath.of(components)
}

internal fun Guild?.asScopeString() = if (this == null) "global scope" else "guild '${this.name}' (${this.id})"

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

internal fun Duration.toTimestampIfFinite(): Instant? =
    takeIfFinite()?.let { Clock.System.now() + it }

fun Duration.takeIfFinite(): Duration? =
    takeIf { it.isFinite() && it.isPositive() }

internal inline fun <reified T : Any> T?.ifNullThrowInternal(message: () -> String): T {
    if (this == null)
        throwInternal(message())
    return this
}

internal class WriteOnce<T : Any>(private val wait: Boolean) : ReadWriteProperty<Any?, T> {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = lock.withLock {
        val value = value
        if (value != null) return value

        if (wait)
            condition.await()
        else
            throwState("Property ${property.name} must be initialized before getting it.")

        return getValue(thisRef, property)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = lock.withLock {
        check(this.value == null) {
            "Cannot set value twice"
        }
        this.value = value
        condition.signalAll()
    }

    internal fun isInitialized(): Boolean = value != null
}
