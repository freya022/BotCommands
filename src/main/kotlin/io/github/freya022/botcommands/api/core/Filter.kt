package io.github.freya022.botcommands.api.core

interface Filter {
    val global: Boolean get() = true
}

internal inline fun <T : Filter> checkFilters(globalFilters: List<T>, commandFilters: List<T>, block: (filter: T) -> Unit) {
    for (it in globalFilters) {
        if (!it.global) continue
        block(it) //Inlined return statements will exit this function
    }
    commandFilters.forEach(block)
}