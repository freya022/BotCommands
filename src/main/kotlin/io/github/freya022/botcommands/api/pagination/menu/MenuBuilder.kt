package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.core.BContext

/**
 * Builds a [Menu]
 *
 * @param E Type of the entries
 */
class MenuBuilder<E>(
    context: BContext,
    entries: List<E>
) : BasicMenuBuilder<E, MenuBuilder<E>, Menu<E>>(context, entries) {
    override fun build(): Menu<E> = Menu(context, this)
}
