package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.pagination.PaginatorComponents
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier
import io.github.freya022.botcommands.api.pagination.TimeoutInfo
import io.github.freya022.botcommands.api.pagination.menu.Menu
import io.github.freya022.botcommands.api.utils.ButtonContent

/**
 * Classic paginator, pages are supplied from [paginator suppliers][PaginatorBuilder.setPaginatorSupplier].
 *
 * You provide the pages, it displays them one by one.
 *
 * Initial page is page 0, there is navigation buttons and an optional delete button
 *
 * **The button IDs used by this paginator and those registered by the [PaginatorComponents] in the [PaginatorSupplier] are cleaned up once the embed is removed with the button**
 *
 * When the message is deleted, you would also have to call [cleanup]
 *
 * @see Menu
 */
class Paginator internal constructor(
    componentsService: Components,
    constraints: InteractionConstraints,
    timeout: TimeoutInfo<Paginator>?,
    override var maxPages: Int,
    supplier: PaginatorSupplier<Paginator>?,
    hasDeleteButton: Boolean,
    firstContent: ButtonContent,
    previousContent: ButtonContent,
    nextContent: ButtonContent,
    lastContent: ButtonContent,
    deleteContent: ButtonContent
) : BasicPaginator<Paginator>(
    componentsService,
    constraints,
    timeout,
    supplier,
    hasDeleteButton,
    firstContent,
    previousContent,
    nextContent,
    lastContent,
    deleteContent
)
