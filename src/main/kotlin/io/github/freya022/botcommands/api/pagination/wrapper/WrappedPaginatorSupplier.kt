package io.github.freya022.botcommands.api.pagination.wrapper

import io.github.freya022.botcommands.api.pagination.AbstractPagination

fun interface WrappedPaginatorSupplier<T : AbstractPagination<T>> {
    fun get(): T
}