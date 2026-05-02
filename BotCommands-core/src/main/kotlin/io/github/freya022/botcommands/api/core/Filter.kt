package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.utils.simpleNestedName

/**
 * Base filter interface, should not be implemented directly.
 */
interface Filter {
    /**
     * Whether this filter is global or command-specific.
     *
     * If `false`, this filter can be applied to specific commands,
     * if `true`, this will be applied to all commands.
     */
    val global: Boolean

    /**
     * Description of the filter, used for logging purposes (like when a filter rejects a command).
     */
    val description: String get() = this.javaClass.simpleNestedName
}

inline fun <T : Filter> checkFilters(globalFilters: List<T>, commandFilters: List<T>, block: (filter: T) -> Unit) {
    val globalFilterIterator = globalFilters.iterator()
    val commandFilterIterator = commandFilters.iterator()
    while (true) {
        val filter = if (globalFilterIterator.hasNext()) {
            globalFilterIterator.next()
        } else if (commandFilterIterator.hasNext()) {
            commandFilterIterator.next()
        } else {
            return
        }
        block(filter) //Inlined return statements will exit this function
    }
}
