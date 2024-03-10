package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components

/**
 * Builds a [Menu]
 *
 * @param E Type of the entries
 */
class MenuBuilder<E>(
    componentsService: Components,
    entries: List<E>
) : BasicMenuBuilder<E, MenuBuilder<E>, Menu<E>>(componentsService, entries) {
    override fun build(): Menu<E> = Menu(componentsService, this)
}
