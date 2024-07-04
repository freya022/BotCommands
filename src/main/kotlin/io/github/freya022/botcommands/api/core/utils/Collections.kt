package io.github.freya022.botcommands.api.core.utils

import java.util.*

fun <T> arrayOfSize(size: Int) = ArrayList<T>(size)

inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)
inline fun <reified T : Enum<T>> enumSetOfAll(): EnumSet<T> = EnumSet.allOf(T::class.java)
inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> = enumSetOf<T>().apply { addAll(elems) }
inline fun <reified T : Enum<T>, V> enumMapOf(): EnumMap<T, V> = EnumMap<T, V>(T::class.java)

fun <T> Collection<T>.unmodifiableView(): Collection<T> {
    return Collections.unmodifiableCollection(this)
}

fun <T> List<T>.toImmutableList(): List<T> {
    return Collections.unmodifiableList(toMutableList())
}

fun <T> List<T>.unmodifiableView(): List<T> {
    return Collections.unmodifiableList(this)
}

fun <T> Set<T>.toImmutableSet(): Set<T> {
    return Collections.unmodifiableSet(toMutableSet())
}

fun <T> Set<T>.unmodifiableView(): Set<T> {
    return Collections.unmodifiableSet(this)
}

fun <K, V> Map<K, V>.toImmutableMap(): Map<K, V> {
    return Collections.unmodifiableMap(LinkedHashMap(this))
}

fun <K, V> Map<K, V>.unmodifiableView(): Map<K, V> {
    return Collections.unmodifiableMap(this)
}

fun <T> Iterable<T>.containsAny(vararg elements: T): Boolean = elements.any { it in this }
fun <T> Iterable<T>.containsAny(elements: Iterable<T>): Boolean = elements.any { it in this }

/**
 * Returns `null` if the element already exists
 */
fun <K, V> MutableMap<K, V>.computeIfAbsentOrNull(key: K, block: (K) -> V): V? {
    if (key !in this) {
        val value = block(key)
        put(key, value)
        return value
    }
    return null
}

/**
 * Returns `null` if the element already exists
 */
fun <K, V> MutableMap<K, V>.putIfAbsentOrNull(key: K, value: V): V? {
    if (key !in this) {
        put(key, value)
        return value
    }
    return null
}