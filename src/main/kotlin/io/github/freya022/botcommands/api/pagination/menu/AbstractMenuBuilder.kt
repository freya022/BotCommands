package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.PageEditor
import io.github.freya022.botcommands.api.pagination.menu.transformer.EntryTransformer
import io.github.freya022.botcommands.api.pagination.menu.transformer.StringTransformer
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginatorBuilder
import net.dv8tion.jda.internal.utils.Checks

/**
 * Provides base for a menu builder
 *
 * @param E Type of the menu entries
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class AbstractMenuBuilder<E, T : AbstractMenuBuilder<E, T, R>, R : AbstractMenu<E, R>> protected constructor(
    context: BContext,
    val entries: List<E>
) : AbstractPaginatorBuilder<T, R>(context) {
    var pageEditor: PageEditor<R>? = null
        private set

    var maxEntriesPerPage: Int = Menu.Defaults.maxEntriesPerPage
        private set

    @Suppress("UNCHECKED_CAST")
    var transformer: EntryTransformer<E> = StringTransformer() as EntryTransformer<E>
        private set

    var rowPrefixSupplier: RowPrefixSupplier = Menu.Defaults.rowPrefixSupplier
        private set

    /**
     * Sets an optional the [PageEditor] for this menu.
     *
     * This allows you to edit the message and/or the embed being built.
     *
     * @return This builder for chaining convenience
     */
    fun setPageEditor(pageEditor: PageEditor<R>?): T = config {
        this.pageEditor = pageEditor
    }

    /**
     * Sets the maximum number of entries per page.
     *
     * If the content of the menu cannot fit all of these entries, they will go to the next page.
     *
     * The default value can be changed in [Menu.Defaults.maxEntriesPerPage].
     *
     * @return This builder for chaining convenience
     */
    fun setMaxEntriesPerPage(maxEntriesPerPage: Int): T = config {
        Checks.positive(maxEntriesPerPage, "Max entries per page")
        this.maxEntriesPerPage = maxEntriesPerPage
    }

    /**
     * Sets the row prefix supplier for this menu.
     *
     * This is the prefix set before appending an entry.
     *
     * The default value can be changed in [Menu.Defaults.rowPrefixSupplier].
     *
     * @return This builder for chaining convenience
     */
    fun setRowPrefixSupplier(rowPrefixSupplier: RowPrefixSupplier): T = config {
        this.rowPrefixSupplier = rowPrefixSupplier
    }

    /**
     * Sets the entry transformer for this menu
     *
     * @param transformer The [EntryTransformer] to use to stringify the entries
     *
     * @return This builder for chaining convenience
     */
    fun setTransformer(transformer: EntryTransformer<E>): T = config {
        this.transformer = transformer
    }
}