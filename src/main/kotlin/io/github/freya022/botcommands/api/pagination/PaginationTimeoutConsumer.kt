package io.github.freya022.botcommands.api.pagination

fun interface PaginationTimeoutConsumer<T : BasicPagination<T>> {
    fun accept(paginator: T)
}
