package io.github.freya022.botcommands.api.pagination.menu.buttonized

import io.github.freya022.botcommands.api.components.event.ButtonEvent

typealias SuspendingChoiceCallback<E> = suspend (event: ButtonEvent, entry: E) -> Unit

/**
 * Callback called when an item has been chosen in a [ButtonMenu]
 *
 * @param E Type of the entries
 */
fun interface BlockingChoiceCallback<E> {
    /**
     * Runs the callback
     *
     * @param event The [ButtonEvent] from the interacting user
     * @param entry The selected entry from the [ButtonMenu] elements
     */
    fun accept(event: ButtonEvent, entry: E)
}
