package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.core.BContext

/**
 * Paginator where pages are made from a list of entries.
 *
 * @param E Type of the entries
 */
class Menu<E> internal constructor(
    context: BContext,
    builder: MenuBuilder<E>
) : BasicMenu<E, Menu<E>>(
    context,
    builder,
    makePages(builder.entries, builder.transformer, builder.rowPrefixSupplier, builder.maxEntriesPerPage)
)
