package io.github.freya022.botcommands.api.pagination.nested

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * A paginator which wraps a paginator, with a select menu to switch between them.
 *
 * @see Paginators.nestedPagination
 */
class NestedPaginator internal constructor(
    context: BContext,
    builder: NestedPaginatorBuilder
) : AbstractNestedPaginator<NestedPaginator>(
    context,
    builder
) {
    object Defaults {
        /** @see AbstractNestedPaginatorBuilder.usePaginatorControls */
        @JvmStatic
        var usePaginatorControls = false
    }
}
