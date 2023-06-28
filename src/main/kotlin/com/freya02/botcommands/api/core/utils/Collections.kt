package com.freya02.botcommands.api.core.utils

import java.util.*

fun <T> arrayOfSize(size: Int) = ArrayList<T>(size)

inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)
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