package io.github.freya022.botcommands.api.pagination

typealias SuspendingPaginationTimeoutConsumer<T> = suspend (T) -> Unit

fun interface BlockingPaginationTimeoutConsumer<T : AbstractPagination<T>> {
    fun accept(paginator: T)
}
