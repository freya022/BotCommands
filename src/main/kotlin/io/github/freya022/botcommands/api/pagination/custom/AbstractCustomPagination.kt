package io.github.freya022.botcommands.api.pagination.custom

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination

/**
 * @param T Type of the implementor
 */
abstract class AbstractCustomPagination<T : AbstractCustomPagination<T>> protected constructor(
    context: BContext,
    builder: AbstractCustomPaginationBuilder<*, T>
) : AbstractPagination<T>(context, builder) {
    abstract var maxPages: Int
        protected set

    /**
     * The page number, after changing this value, you can update the message with the new content from [getCurrentMessage].
     *
     * The page must be between `0` and [`maxPages - 1`][maxPages]
     */
    var page: Int = 0
        set(value) {
            // 0 <= value < maxPages
            require(value in 0..<maxPages) {
                "Page needs to be between 0 and $maxPages (excluded)"
            }
            field = value
        }

    val isFirstPage: Boolean
        get() = page == 0
    val isLastPage: Boolean
        get() = page >= maxPages - 1
}
