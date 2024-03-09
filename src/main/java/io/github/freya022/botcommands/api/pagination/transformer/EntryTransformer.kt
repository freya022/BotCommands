package io.github.freya022.botcommands.api.pagination.transformer

/**
 * Interface to transform pagination entries into strings
 *
 * @param T Type of the pagination entry
 */
fun interface EntryTransformer<in T> {
    fun toString(entry: T): String
}
