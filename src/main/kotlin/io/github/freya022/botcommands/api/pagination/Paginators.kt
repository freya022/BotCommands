package io.github.freya022.botcommands.api.pagination

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.RequiresComponents
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.pagination.custom.CustomPageEditor
import io.github.freya022.botcommands.api.pagination.custom.CustomPagination
import io.github.freya022.botcommands.api.pagination.custom.CustomPaginationBuilder
import io.github.freya022.botcommands.api.pagination.menu.AbstractMenuBuilder
import io.github.freya022.botcommands.api.pagination.menu.MenuBuilder
import io.github.freya022.botcommands.api.pagination.menu.buttonized.BlockingChoiceCallback
import io.github.freya022.botcommands.api.pagination.menu.buttonized.ButtonMenu
import io.github.freya022.botcommands.api.pagination.menu.buttonized.ButtonMenuBuilder
import io.github.freya022.botcommands.api.pagination.menu.buttonized.SuspendingChoiceCallback
import io.github.freya022.botcommands.api.pagination.nested.NestedPaginatorBuilder
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginator
import io.github.freya022.botcommands.api.pagination.paginator.Paginator
import io.github.freya022.botcommands.api.pagination.paginator.PaginatorBuilder

/**
 * Factory for multiple pagination types.
 *
 * ### Resource usage
 *
 * Since paginators heavily rely on *ephemeral* components, they might consume more memory if not cleaned up.
 *
 * In case the paginator is deleted, calling [AbstractPaginator.cleanup] allows cleaning up early.
 * If this is not called, it will be done when the paginator expires.
 *
 * By default, components used in a page are invalidated
 * when a [new page is requested][AbstractPagination.getCurrentMessage].
 * In case you want to reuse components, you can make the components be only invalidated once the pagination expires
 * by disabling [AbstractPaginationBuilder.cleanAfterRefresh].
 *
 * ### Timeout
 *
 * Paginators have a default timeout set to [Components.defaultTimeout],
 * which can be modified using [AbstractPaginationBuilder.setTimeout].
 *
 * You can also disable the timeout using [AbstractPaginationBuilder.noTimeout],
 * in which case you will need to clean up the components manually.
 */
@BService
@Dependencies(Components::class)
@RequiresComponents
class Paginators(private val context: BContext) {
    /**
     * A single page generated by a [CustomPageEditor].
     */
    fun singlePage(pageEditor: CustomPageEditor<CustomPagination>): CustomPaginationBuilder =
        CustomPaginationBuilder(context, 1, pageEditor)

    /**
     * Classic paginator, where each page is generated by a [CustomPageEditor].
     */
    fun customPagination(maxPages: Int, pageEditor: CustomPageEditor<CustomPagination>): CustomPaginationBuilder =
        CustomPaginationBuilder(context, maxPages, pageEditor)

    /**
     * Classic paginator, where each page is generated by a [PageEditor].
     *
     * In addition to the content provided, five buttons (first, previous, next, last, (optional) delete)
     * are added to navigate from page to page.
     */
    fun paginator(maxPages: Int, pageEditor: PageEditor<Paginator>): PaginatorBuilder =
        PaginatorBuilder(context, maxPages, pageEditor)

    /**
     * A paginator where each page is filled with a list of entries.
     *
     * Each page can be limited to [a specified number of entries][AbstractMenuBuilder.maxEntriesPerPage].
     *
     * Each entry can have its [prefix][AbstractMenuBuilder.rowPrefixSupplier]
     * and its [string representation][AbstractMenuBuilder.transformer] customized.
     */
    fun <E> menu(entries: List<E>): MenuBuilder<E> =
        MenuBuilder(context, entries)

    /**
     * A paginator where each page is filled with a list of entries.
     *
     * Each page can be limited to [a specified number of entries][AbstractMenuBuilder.maxEntriesPerPage].
     *
     * Each entry can have its [prefix][AbstractMenuBuilder.rowPrefixSupplier]
     * and its [string representation][AbstractMenuBuilder.transformer] customized.
     *
     * In addition, each entry is associated to a [Button],
     * when clicked, the [callback][ButtonMenuBuilder.callback] is run.
     */
    @JvmName("buttonMenu")
    final fun <E> buttonMenuJava(entries: List<E>, buttonContentSupplier: ButtonMenu.ButtonContentSupplier<E>, callback: BlockingChoiceCallback<E>): ButtonMenuBuilder<E> =
        ButtonMenuBuilder(context, entries, buttonContentSupplier, callback::accept)

    /**
     * A paginator where each page is filled with a list of entries.
     *
     * Each page can be limited to [a specified number of entries][AbstractMenuBuilder.maxEntriesPerPage].
     *
     * Each entry can have its [prefix][AbstractMenuBuilder.rowPrefixSupplier]
     * and its [string representation][AbstractMenuBuilder.transformer] customized.
     *
     * In addition, each entry is associated to a [Button],
     * when clicked, the [callback][ButtonMenuBuilder.callback] is run.
     */
    @JvmSynthetic
    fun <E> buttonMenu(entries: List<E>, buttonContentSupplier: ButtonMenu.ButtonContentSupplier<E>, callback: SuspendingChoiceCallback<E>): ButtonMenuBuilder<E> =
        ButtonMenuBuilder(context, entries, buttonContentSupplier, callback)

    /**
     * A paginator which wraps a paginator, with a select menu to switch between them.
     */
    fun nestedPagination(): NestedPaginatorBuilder =
        NestedPaginatorBuilder(context)
}