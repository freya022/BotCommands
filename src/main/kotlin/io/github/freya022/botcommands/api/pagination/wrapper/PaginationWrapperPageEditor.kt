package io.github.freya022.botcommands.api.pagination.wrapper

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 * @param T Type of the pagination wrapper instance
 *
 * @see accept
 */
fun interface PaginationWrapperPageEditor<T : AbstractPaginationWrapper<T, *>> {
    /**
     * Edits the page being created.
     *
     * You can also use the pagination wrapper instance to:
     * - Modify the pagination wrapper state when a button is triggered
     * - Delete the menu, cancel the timeout and clean up the components when a button is clicked
     *
     * @param paginationWrapper The wrapper instance this supplier is for
     * @param builder           The [MessageCreateBuilder] for this pagination wrapper
     */
    fun accept(paginationWrapper: T, builder: MessageCreateBuilder)
}
