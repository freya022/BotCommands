package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.pagination.PageEditor

/**
 * Builds a [Paginator]
 */
class PaginatorBuilder internal constructor(
    componentsService: Components,
    val pageEditor: PageEditor<Paginator>
) : BasicPaginatorBuilder<PaginatorBuilder, Paginator>(componentsService) {
    var maxPages = 0
        private set(value) {
            check(maxPages > 0) { "Max pages must be > 0" }
            field = value
        }

    fun setMaxPages(maxPages: Int): PaginatorBuilder = config {
        this.maxPages = maxPages
    }

    override fun build(): Paginator = Paginator(componentsService, this)
}
