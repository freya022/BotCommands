package io.github.freya022.botcommands.api.pagination

import java.util.concurrent.TimeUnit

data class TimeoutInfo<T : BasicPagination<T>>(
    val timeout: Long,
    val unit: TimeUnit,
    val onTimeout: PaginationTimeoutConsumer<T>
) 