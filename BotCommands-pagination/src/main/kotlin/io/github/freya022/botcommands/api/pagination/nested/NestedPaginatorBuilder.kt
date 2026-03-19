package io.github.freya022.botcommands.api.pagination.nested

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * Builds a [NestedPaginator].
 *
 * @see Paginators.nestedPagination
 */
class NestedPaginatorBuilder internal constructor(
    context: BContext
) : AbstractNestedPaginatorBuilder<NestedPaginatorBuilder, NestedPaginator>(context) {
    override fun build(): NestedPaginator = NestedPaginator(context, this)
}
