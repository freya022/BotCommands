package io.github.freya022.botcommands.api.pagination.menu

fun interface RowPrefixSupplier {
    fun apply(entryNum: Int, maxEntries: Int): String
}
