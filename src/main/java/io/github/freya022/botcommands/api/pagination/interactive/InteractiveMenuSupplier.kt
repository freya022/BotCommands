package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.pagination.PaginatorComponents
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder

/**
 * @param T Type of the interactive menu instance
 *
 * @see get
 */
fun interface InteractiveMenuSupplier<T : BasicInteractiveMenu<T>> {
    /**
     * Returns the [MessageEmbed] for this interactive menu's page
     *
     * You can also use the interactive menu instance to:
     * - Modify the interactive menu's state when a button is triggered
     * - Delete the menu, cancel the timeout and clean up the components when a button is clicked
     *
     *
     * @param interactiveMenu The interaction menu instance this interactive menu supplier is for
     * @param pageNumber      The page number of the currently displayed menu
     * @param editBuilder     The [MessageEditBuilder] for this interactive menu, you can mostly ignore it but can use it to add attachments for examples, to use them in your embeds
     * @param components      The [PaginatorComponents] for this interactive menu's page, this allows you to add components on this page.
     * **Do not use [MessageEditBuilder.setComponents] and such, the menu will override these**
     *
     * @return A [MessageEmbed] for this interactive menu's page
     */
    fun get(interactiveMenu: T, pageNumber: Int, editBuilder: MessageEditBuilder, components: PaginatorComponents): MessageEmbed
}
