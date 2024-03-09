package io.github.freya022.botcommands.api.pagination

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder

/**
 * @param T Type of the paginator instance
 *
 * @see get
 */
fun interface PaginatorSupplier<T> {
    /**
     * Returns the [MessageEmbed] for this paginator page
     *
     * You can also use the paginator instance to:
     * - Modify the pagination's state when a button is triggered
     * - Delete the pagination, cancel the timeout and clean up the components when a button is clicked
     *
     * @param paginator   The paginator instance this is for
     * @param editBuilder The [MessageEditBuilder] for this interactive menu, you can mostly ignore it but can use it to add attachments for examples, to use them in your embeds
     * @param components  The [PaginatorComponents] for this interactive menu's page, this allows you to add components on this page.
     * **Do not use [MessageEditBuilder.setComponents] and such, the menu will override these**
     *
     * @return A [MessageEmbed] for this interactive menu's page
     */
    fun get(paginator: T, editBuilder: MessageEditBuilder, components: PaginatorComponents, page: Int): MessageEmbed
}
