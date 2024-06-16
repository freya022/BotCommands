package io.github.freya022.botcommands.api.commands

/**
 * May hold filters.
 */
interface IFilterContainer {
    /**
     * Return `true` if this has a one or more filters.
     */
    fun hasFilters(): Boolean
}