package io.github.freya022.botcommands.api.pagination.transformer

class StringTransformer : EntryTransformer<Any> {
    override fun toString(entry: Any): String = entry.toString()
}
