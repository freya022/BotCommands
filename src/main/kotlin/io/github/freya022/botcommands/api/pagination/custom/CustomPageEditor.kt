package io.github.freya022.botcommands.api.pagination.custom

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 * @param T Type of the paginator instance
 *
 * @see accept
 */
fun interface CustomPageEditor<T> {
    /**
     * Edits the page being created.
     *
     * You can also use the paginator instance to:
     * - Modify the pagination's state when a button is triggered
     * - Delete the pagination, cancel the timeout and clean up the components when a button is clicked
     *
     * @param paginator    The paginator instance this is for
     * @param builder      The [MessageCreateBuilder] for the current page
     * @param page         The page number of the currently displayed paginator
     */
    fun accept(paginator: T, builder: MessageCreateBuilder, page: Int)
}
