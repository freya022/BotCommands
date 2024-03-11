package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginatorBuilder
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

/**
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class AbstractInteractiveMenuBuilder<T : AbstractInteractiveMenuBuilder<T, R>, R : AbstractInteractiveMenu<R>> protected constructor(
    context: BContext
) : AbstractPaginatorBuilder<T, R>(context) {
    val items: MutableList<InteractiveMenuItem<R>> = ArrayList()
    var usePaginator: Boolean = false
        private set

    /**
     * Adds a menu to this [InteractiveMenu].
     *
     * **Note:** The first added menu will be the first selected one.
     *
     * @param content  The content of the [SelectOption] bound to this menu
     * @param supplier The interactive menu supplier for this menu's page
     *
     * @return This builder for chaining convenience
     *
     * @see SelectContent.of
     */
    fun addMenu(content: SelectContent, maxPages: Int, supplier: InteractiveMenuPageEditor<R>): T = config {
        items.add(InteractiveMenuItem(content, maxPages, supplier))
    }

    /**
     * Sets whether the paginator buttons (previous, next, delete, etc...) should appear with this interactive menu.
     *
     * This is disabled by default.
     *
     * @param usePaginator `true` to use the paginator buttons
     *
     * @return This builder for chaining convenience
     */
    fun usePaginator(usePaginator: Boolean): T = config {
        this.usePaginator = usePaginator
    }
}
