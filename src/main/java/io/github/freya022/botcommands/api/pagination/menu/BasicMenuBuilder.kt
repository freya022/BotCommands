package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.pagination.PageEditor
import io.github.freya022.botcommands.api.pagination.paginator.BasicPaginatorBuilder
import io.github.freya022.botcommands.api.pagination.transformer.EntryTransformer
import io.github.freya022.botcommands.api.pagination.transformer.StringTransformer
import net.dv8tion.jda.internal.utils.Checks
import kotlin.math.floor
import kotlin.math.log10

/**
 * Provides base for a menu builder
 *
 * @param E Type of the menu entries
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class BasicMenuBuilder<E, T : BasicMenuBuilder<E, T, R>, R : BasicMenu<E, R>> protected constructor(
    componentsService: Components,
    val entries: List<E>
) : BasicPaginatorBuilder<T, R>(componentsService) {
    var pageEditor: PageEditor<R>? = null
        private set

    var maxEntriesPerPage: Int = 5
        private set

    @Suppress("UNCHECKED_CAST")
    var transformer: EntryTransformer<E> = StringTransformer() as EntryTransformer<E>
        private set

    var rowPrefixSupplier: RowPrefixSupplier = RowPrefixSupplier { entryNum: Int, maxEntry: Int ->
        val spaces = getPadding(entryNum, maxEntry)
        "`" + " ".repeat(spaces) + entryNum + ".` "
    }
        private set

    fun setPageEditor(pageEditor: PageEditor<R>): T = config {
        this.pageEditor = pageEditor
    }

    /**
     * Sets the maximum number of entries per page
     * **This does not mean there will be X entries per page** but rather it will try to fit 5 entries maximum per page, if some text is too long it'll cut down the number of entries
     *
     * @param maxEntriesPerPage The maximum amount of entries per page
     *
     * @return This builder for chaining convenience
     */
    fun setMaxEntriesPerPage(maxEntriesPerPage: Int): T = config {
        Checks.positive(maxEntriesPerPage, "Max entries per page")
        this.maxEntriesPerPage = maxEntriesPerPage
    }

    /**
     * Sets the row prefix supplier for this menu
     *
     * This is what gets printed before each entry when it gets displayed
     *
     * @param rowPrefixSupplier The row prefix supplier, the first parameter is the entry number, and the second parameter is the max entries number
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

    companion object {
        /**
         * Returns the padding needed between this entry number and the maximum entry number
         *
         * @param entryNum The current entry number
         * @param maxEntry The maximum entry number
         *
         * @return The number of padding spaces needed
         */
        @JvmStatic
        fun getPadding(entryNum: Int, maxEntry: Int): Int {
            val entryDigits = floor(log10(entryNum.toDouble()) + 1)
            val maxEntryDigits = floor(log10(maxEntry.toDouble()) + 1)
            return (maxEntryDigits - entryDigits).toInt()
        }
    }
}