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
    val buttonContentSupplier: ButtonMenu.ButtonContentSupplier<E>,
    val callback: SuspendingChoiceCallback<E>
) : AbstractMenuBuilder<E, ButtonMenuBuilder<E>, ButtonMenu<E>>(context, entries) {
    var reusable: Boolean = ButtonMenu.Defaults.reusable
        private set

    /**
     * Sets whether selecting an entry invalidates this menu.
     *
     * If `true`, the buttons will only be usable once,
     * if `false`, the buttons can be clicked and callback ran multiple times.
     */
    fun setReusable(reusable: Boolean): ButtonMenuBuilder<E> = config {
        this.reusable = reusable
    }

    override fun build(): ButtonMenu<E> = ButtonMenu(context, this)
}
