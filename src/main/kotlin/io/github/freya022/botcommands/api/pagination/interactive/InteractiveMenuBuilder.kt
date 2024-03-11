package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * Builds an [InteractiveMenu].
 *
 * @see Paginators.interactionMenu
 */
class InteractiveMenuBuilder internal constructor(
    context: BContext
) : BasicInteractiveMenuBuilder<InteractiveMenuBuilder, InteractiveMenu>(context) {
    override fun build(): InteractiveMenu = InteractiveMenu(context, this)
}
