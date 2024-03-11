package io.github.freya022.botcommands.api.pagination.wrapper

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * Builds a [PaginationWrapper].
 *
 * @see Paginators.paginationWrapper
 */
class PaginationWrapperBuilder<W : AbstractPagination<W>> internal constructor(
    context: BContext
) : AbstractPaginationWrapperBuilder<PaginationWrapperBuilder<W>, PaginationWrapper<W>, W>(context) {
    override fun build(): PaginationWrapper<W> = PaginationWrapper(context, this)
}
