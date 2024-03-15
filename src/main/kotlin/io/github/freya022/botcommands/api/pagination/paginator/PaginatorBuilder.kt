package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.PageEditor
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * Builds a [Paginator].
 *
 * @see Paginators.paginator
 */
class PaginatorBuilder internal constructor(
    context: BContext,
    val maxPages: Int,
    val pageEditor: PageEditor<Paginator>
) : AbstractPaginatorBuilder<PaginatorBuilder, Paginator>(context) {
    override fun build(): Paginator = Paginator(context, this)
}
