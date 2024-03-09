package io.github.freya022.botcommands.api.pagination

import net.dv8tion.jda.api.entities.Message

fun interface PaginationTimeoutConsumer<T : BasicPagination<T>> {
    fun accept(paginator: T, message: Message?)
}
