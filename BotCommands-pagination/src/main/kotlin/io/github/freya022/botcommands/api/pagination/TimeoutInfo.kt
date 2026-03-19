package io.github.freya022.botcommands.api.pagination

import kotlin.time.Duration

class TimeoutInfo<T : AbstractPagination<T>>(
    val timeout: Duration,
    val onTimeout: (suspend (T) -> Unit)?
) 
