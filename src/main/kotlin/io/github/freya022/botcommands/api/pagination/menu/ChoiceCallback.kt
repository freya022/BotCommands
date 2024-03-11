package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.event.ButtonEvent

typealias SuspendingChoiceCallback<E> = suspend (event: ButtonEvent, entry: E) -> Unit

/**
 * Callback called when an item has been chosen in a [ChoiceMenu]
 *
 * @param E Type of the entries
 */
fun interface BlockingChoiceCallback<E> {
    /**
     * Runs the callback
     *
     * @param event The [ButtonEvent] from the interacting user
     * @param entry The selected entry from the [ChoiceMenu] elements
     */
    fun accept(event: ButtonEvent, entry: E)
}
