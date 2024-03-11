package io.github.freya022.botcommands.api.pagination.wrapper

import io.github.freya022.botcommands.api.components.utils.SelectContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import io.github.freya022.botcommands.api.pagination.AbstractPaginationBuilder
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

/**
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 * @param W Type of the wrapped pagination
 */
abstract class AbstractPaginationWrapperBuilder<T : AbstractPaginationWrapperBuilder<T, R, W>, R : AbstractPaginationWrapper<R, W>, W : AbstractPagination<W>> protected constructor(
    context: BContext
) : AbstractPaginationBuilder<T, R>(context) {
    val items: MutableList<WrappedPaginationItem<W>> = ArrayList()

    var editor: PaginationWrapperPageEditor<R>? = null

    /**
     * Adds a menu to this pagination wrapper.
     *
     * @param content           The content of the [SelectOption] bound to this wrapper
     * @param paginatorSupplier The supplier returning the paginator which is wrapped by this pagination
     *
     * @return This builder for chaining convenience
     *
     * @see SelectContent.of
     */
    fun addMenu(content: SelectContent, paginatorSupplier: WrappedPaginatorSupplier<W>): T = config {
        items.add(WrappedPaginationItem(content, paginatorSupplier))
    }
}
