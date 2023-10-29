package io.github.freya022.botcommands.api.core.utils

import java.util.*

fun <T> arrayOfSize(size: Int) = ArrayList<T>(size)

inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)
inline fun <reified T : Enum<T>> enumSetOfAll(): EnumSet<T> = EnumSet.allOf(T::class.java)
inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> = enumSetOf<T>().apply { addAll(elems) }
inline fun <reified T : Enum<T>, V> enumMapOf(): EnumMap<T, V> = EnumMap<T, V>(T::class.java)

fun <T> List<T>.toImmutableList(): List<T> {
    return Collections.unmodifiableList(toMutableList())
}

fun <T> Set<T>.toImmutableSet(): Set<T> {
    return Collections.unmodifiableSet(toMutableSet())
}

fun <K, V> Map<K, V>.toImmutableMap(): Map<K, V> {
    return Collections.unmodifiableMap(LinkedHashMap(this))
}

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