package dev.freya02.botcommands.jda.ktx.components.utils

import java.util.*

@PublishedApi
internal inline fun <reified T : Enum<T>> Array<out T>.toEnumSet(): EnumSet<T> {
    val set = EnumSet.noneOf(T::class.java)
    return set.apply { addAll(this@toEnumSet) }
}
