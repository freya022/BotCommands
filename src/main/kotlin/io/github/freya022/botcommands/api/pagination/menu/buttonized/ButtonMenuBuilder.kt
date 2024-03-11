package io.github.freya022.botcommands.api.pagination.menu.buttonized

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators
import io.github.freya022.botcommands.api.pagination.menu.AbstractMenuBuilder

/**
 * Builds a [ButtonMenu].
 *
 * @param E Type of the entries
 *
 * @see Paginators.buttonMenu
 */
class ButtonMenuBuilder<E> internal constructor(
    context: BContext,
    entries: List<E>,
    val buttonContentSupplier: ButtonContentSupplier<E>,
    val callback: SuspendingChoiceCallback<E>
) : AbstractMenuBuilder<E, ButtonMenuBuilder<E>, ButtonMenu<E>>(context, entries) {
    override fun build(): ButtonMenu<E> = ButtonMenu(context, this)
}
