package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.components.utils.ButtonContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import io.github.freya022.botcommands.api.pagination.AbstractPaginationBuilder

/**
 * Most basic paginator builder.
 *
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class AbstractPaginatorBuilder<T : AbstractPaginationBuilder<T, R>, R : AbstractPagination<R>>(
    context: BContext
) : AbstractPaginationBuilder<T, R>(context) {
    var firstContent: ButtonContent = Paginator.Defaults.firstPageButtonContent
        private set
    var previousContent: ButtonContent = Paginator.Defaults.previousPageButtonContent
        private set
    var nextContent: ButtonContent = Paginator.Defaults.nextPageButtonContent
        private set
    var lastContent: ButtonContent = Paginator.Defaults.lastPageButtonContent
        private set
    var deleteContent: ButtonContent = Paginator.Defaults.deleteButtonContent
        private set

    var hasDeleteButton: Boolean = false
        private set

    /**
     * Specifies whether this paginator should have a delete button.
     *
     * The button can only be used by those satisfying [the constraints][setConstraints].
     *
     * The default value can be changed in [Paginator.Defaults.useDeleteButton].
     *
     * @return This builder for chaining convenience
     *
     * @see setConstraints
     */
    fun useDeleteButton(hasDeleteButton: Boolean): T = config {
        this.hasDeleteButton = hasDeleteButton
    }

    /**
     * Sets the content for the button which goes to the first page
     *
     * The default value can be changed in [Paginator.Defaults.firstPageButtonContent].
     *
     * @return This builder for chaining convenience
     */
    fun setFirstContent(firstContent: ButtonContent): T = config {
        this.firstContent = firstContent
    }

    /**
     * Sets the content for the button which goes to the previous page
     *
     * The default value can be changed in [Paginator.Defaults.previousPageButtonContent].
     *
     * @return This builder for chaining convenience
     */
    fun setPreviousContent(previousContent: ButtonContent): T = config {
        this.previousContent = previousContent
    }

    /**
     * Sets the content for the button which goes to the next page
     *
     * The default value can be changed in [Paginator.Defaults.nextPageButtonContent].
     *
     * @return This builder for chaining convenience
     */
    fun setNextContent(nextContent: ButtonContent): T = config {
        this.nextContent = nextContent
    }

    /**
     * Sets the content for the button which goes to the last page
     *
     * The default value can be changed in [Paginator.Defaults.lastPageButtonContent].
     *
     * @return This builder for chaining convenience
     */
    fun setLastContent(lastContent: ButtonContent): T = config {
        this.lastContent = lastContent
    }

    /**
     * Sets the content for the button which deletes this paginator
     *
     * The default value can be changed in [Paginator.Defaults.deleteButtonContent].
     *
     * @return This builder for chaining convenience
     */
    fun setDeleteContent(deleteContent: ButtonContent): T = config {
        this.deleteContent = deleteContent
    }
}
