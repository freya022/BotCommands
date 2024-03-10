package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.pagination.paginator.Paginator

/**
 * Paginator where pages are made from a list of entries.
 *
 * @param E Type of the entries
 *
 * @see Paginator
 * @see ChoiceMenu
 */
class Menu<E> internal constructor(
    componentsService: Components,
    builder: MenuBuilder<E>
) : BasicMenu<E, Menu<E>>(
    componentsService,
    builder,
    makePages(builder.entries, builder.transformer, builder.rowPrefixSupplier, builder.maxEntriesPerPage)
)
