package io.github.freya022.botcommands.api.pagination.wrapper

import io.github.freya022.botcommands.api.components.utils.SelectContent
import io.github.freya022.botcommands.api.pagination.AbstractPagination

data class WrappedPaginationItem<T : AbstractPagination<T>>(
    val content: SelectContent,
    private val paginatorSupplier: WrappedPaginatorSupplier<T>
) {
    val wrappedPagination: T by lazy(paginatorSupplier::get)
}