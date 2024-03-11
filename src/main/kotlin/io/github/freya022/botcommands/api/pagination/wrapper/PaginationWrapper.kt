package io.github.freya022.botcommands.api.pagination.wrapper

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 *A paginator which wraps other paginators, with a select menu to switch between them.
 *
 * @see Paginators.paginationWrapper
 */
class PaginationWrapper<W : AbstractPagination<W>> internal constructor(
    context: BContext,
    builder: PaginationWrapperBuilder<W>
) : AbstractPaginationWrapper<PaginationWrapper<W>, W>(
    context,
    builder
)
