package io.github.freya022.botcommands.api.pagination.interactive

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 * @param T Type of the interactive menu instance
 *
 * @see accept
 */
fun interface InteractiveMenuPageEditor<T : AbstractInteractiveMenu<T>> {
    /**
     * Edits the page being created.
     *
     * You can also use the interactive menu instance to:
     * - Modify the interactive menu's state when a button is triggered
     * - Delete the menu, cancel the timeout and clean up the components when a button is clicked
     *
     * @param interactiveMenu The interaction menu instance this interactive menu supplier is for
     * @param builder         The [MessageCreateBuilder] for this interactive menu
     * @param pageNumber      The page number of the currently displayed menu
     */
    fun accept(interactiveMenu: T, builder: MessageCreateBuilder, pageNumber: Int)
}
