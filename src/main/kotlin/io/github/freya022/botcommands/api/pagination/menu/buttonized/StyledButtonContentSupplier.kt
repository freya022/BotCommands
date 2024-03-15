package io.github.freya022.botcommands.api.pagination.menu.buttonized

import io.github.freya022.botcommands.api.components.utils.ButtonContent

/**
 * A [ButtonContent] supplier for use in different paginators,
 * allowing you to use your own text and emojis on buttons.
 *
 * @param T Item type
 *
 * @see ButtonContent.fromLabel
 * @see ButtonContent.fromEmoji
 */
fun interface StyledButtonContentSupplier<T> {
    /**
     * Returns a [ButtonContent] based on the given item and the current page number of the paginator
     *
     * @param item  The item bound to this button
     * @param index The index of this item on the current page number of the paginator
     * @return The [ButtonContent] of this item
     */
    fun apply(item: T, index: Int): StyledButtonContent
}
