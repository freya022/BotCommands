package io.github.freya022.botcommands.api.pagination

import kotlin.time.Duration

data class TimeoutInfo<T : BasicPagination<T>>(
    val timeout: Duration,
    val onTimeout: suspend (T) -> Unit
) 