package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier

/**
 * Builds an [InteractiveMenu]
 */
class InteractiveMenuBuilder internal constructor(
    componentsService: Components
) : BasicInteractiveMenuBuilder<InteractiveMenuBuilder, InteractiveMenu>(componentsService) {
    override fun build(): InteractiveMenu = InteractiveMenu(componentsService, this)

    override fun setPaginatorSupplier(paginatorSupplier: PaginatorSupplier<InteractiveMenu>): Nothing {
        throw IllegalStateException("Interactive menu builder cannot have a PaginatorSupplier")
    }
}
