package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * Builds a [Menu].
 *
 * @param E Type of the entries
 *
 * @see Paginators.menu
 */
class MenuBuilder<E> internal constructor(
    context: BContext,
    entries: List<E>
) : AbstractMenuBuilder<E, MenuBuilder<E>, Menu<E>>(context, entries) {
    override fun build(): Menu<E> = Menu(context, this)
}
