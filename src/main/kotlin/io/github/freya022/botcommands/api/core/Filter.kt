package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.utils.simpleNestedName

/**
 * Base filter interface.
 *
 * @see TextCommandFilter
 * @see ApplicationCommandFilter
 * @see ComponentInteractionFilter
 */
interface Filter {
    /**
     * Whether this filter is global or command-specific.
     *
     * - Command-specific filters must override this to `false`.
     * - Global filters cannot be used on specific commands/components
     *
     * **Default:** `true`
     */
    val global: Boolean get() = true

    /**
     * Description of the filter, used for logging purposes (like when a filter rejects a command).
     */
    val description: String get() = this.javaClass.simpleNestedName
}

internal inline fun <T : Filter> checkFilters(globalFilters: List<T>, commandFilters: List<T>, block: (filter: T) -> Unit) {
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