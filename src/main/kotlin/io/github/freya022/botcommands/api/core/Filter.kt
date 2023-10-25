package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.utils.simpleNestedName

interface Filter {
    val global: Boolean get() = true

    val description: String get() = this.javaClass.simpleNestedName
}

internal inline fun <T : Filter> checkFilters(globalFilters: List<T>, commandFilters: List<T>, block: (filter: T) -> Unit) {
    globalFilters.forEach(block) //Inlined return statements will exit this function
    commandFilters.forEach(block)
}