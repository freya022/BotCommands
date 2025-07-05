package io.github.freya022.botcommands.api.pagination.nested

import io.github.freya022.botcommands.api.components.utils.SelectContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.PageEditor
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginatorBuilder
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

/**
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class AbstractNestedPaginatorBuilder<T : AbstractNestedPaginatorBuilder<T, R>, R : AbstractNestedPaginator<R>> protected constructor(
    context: BContext
) : AbstractPaginatorBuilder<T, R>(context) {
    var usePaginatorControls: Boolean = NestedPaginator.Defaults.usePaginatorControls
        private set

    val items: MutableList<NestedPaginationItem<R>> = ArrayList()

    /**
     * Whether to display the buttons to navigate in a nested pagination
     *
     * The default value can be changed in [NestedPaginator.Defaults.usePaginatorControls].
     */
    fun usePaginatorControls(usePaginatorControls: Boolean): T = config {
        this.usePaginatorControls = usePaginatorControls
    }

    /**
     * Adds a menu to this pagination wrapper.
     *
     * @param content    The content of the [SelectOption] bound to this wrapper
     * @param maxPages   How many pages the page editor supports
     * @param pageEditor The supplier returning the paginator which is wrapped by this pagination
     *
     * @return This builder for chaining convenience
     *
     * @see SelectContent.of
     */
    fun addMenu(content: SelectContent, maxPages: Int, pageEditor: PageEditor<R>): T = config {
        items.add(NestedPaginationItem(content, maxPages, pageEditor))
    }
}
