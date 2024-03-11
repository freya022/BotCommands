package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * Builds a [ChoiceMenu].
 *
 * @param E Type of the entries
 *
 * @see Paginators.choiceMenu
 */
class ChoiceMenuBuilder<E> internal constructor(
    context: BContext,
    entries: List<E>,
    val buttonContentSupplier: ButtonContentSupplier<E>,
    val callback: SuspendingChoiceCallback<E>
) : BasicMenuBuilder<E, ChoiceMenuBuilder<E>, ChoiceMenu<E>>(context, entries) {
    override fun build(): ChoiceMenu<E> = ChoiceMenu(context, this)
}
