package dev.freya02.botcommands.jda.ktx.components.utils

import java.util.*

@PublishedApi
internal inline fun <reified T : Enum<T>> enumSetOf(vararg enum: T): EnumSet<T> {
    val set = EnumSet.noneOf(T::class.java)
    set.addAll(enum)
    return set
}
