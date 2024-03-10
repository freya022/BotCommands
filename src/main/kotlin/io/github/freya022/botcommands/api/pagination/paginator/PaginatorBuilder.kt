package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.PageEditor

/**
 * Builds a [Paginator]
 */
class PaginatorBuilder internal constructor(
    context: BContext,
    val pageEditor: PageEditor<Paginator>
) : BasicPaginatorBuilder<PaginatorBuilder, Paginator>(context) {
    var maxPages = 0
        private set(value) {
            check(maxPages > 0) { "Max pages must be > 0" }
            field = value
        }

    fun setMaxPages(maxPages: Int): PaginatorBuilder = config {
        this.maxPages = maxPages
    }

    override fun build(): Paginator = Paginator(context, this)
}
