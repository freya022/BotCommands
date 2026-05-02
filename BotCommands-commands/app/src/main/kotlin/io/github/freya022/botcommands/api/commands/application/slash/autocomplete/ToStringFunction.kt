package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

fun interface ToStringFunction<T> {
    fun toString(item: T): String
}
