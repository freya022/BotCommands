package io.github.freya022.botcommands.api.pagination.nested

import io.github.freya022.botcommands.api.components.utils.SelectContent
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import io.github.freya022.botcommands.api.pagination.PageEditor

data class NestedPaginationItem<T : AbstractPagination<T>>(
    val content: SelectContent,
    val maxPages: Int,
    val pageEditor: PageEditor<T>
) {
    var page = 0
}