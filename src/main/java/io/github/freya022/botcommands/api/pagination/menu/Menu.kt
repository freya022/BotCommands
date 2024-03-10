package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier
import io.github.freya022.botcommands.api.pagination.TimeoutInfo
import io.github.freya022.botcommands.api.pagination.paginator.Paginator
import io.github.freya022.botcommands.api.pagination.transformer.EntryTransformer
import io.github.freya022.botcommands.api.utils.ButtonContent

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
    constraints: InteractionConstraints,
    timeout: TimeoutInfo<Menu<E>>?,
    hasDeleteButton: Boolean,
    firstContent: ButtonContent,
    previousContent: ButtonContent,
    nextContent: ButtonContent,
    lastContent: ButtonContent,
    deleteContent: ButtonContent,
    entries: List<E>,
    maxEntriesPerPage: Int,
    transformer: EntryTransformer<E>,
    rowPrefixSupplier: RowPrefixSupplier?,
    supplier: PaginatorSupplier<Menu<E>>?
) : BasicMenu<E, Menu<E>>(
    componentsService,
    constraints,
    timeout,
    hasDeleteButton,
    firstContent,
    previousContent,
    nextContent,
    lastContent,
    deleteContent,
    makePages(entries, transformer, rowPrefixSupplier!!, maxEntriesPerPage),
    supplier
)
