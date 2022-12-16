package com.freya02.botcommands.api.pagination.paginator;

import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.pagination.BasicPagination;
import com.freya02.botcommands.api.pagination.BasicPaginationBuilder;
import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.utils.ButtonContent;
import org.jetbrains.annotations.NotNull;

/**
 * Provides base for a paginator builder
 *
 * @param <T> Type of the implementor
 * @param <R> Type of the implementor {@link #build()} return type
 */
@SuppressWarnings("unchecked")
public abstract class BasicPaginatorBuilder<T extends BasicPaginationBuilder<T, R>, R extends BasicPagination<R>> extends BasicPaginationBuilder<T, R> {
	private static final ButtonContent DEFAULT_FIRST_CONTENT = ButtonContent.withShortcode("rewind");
	private static final ButtonContent DEFAULT_PREVIOUS_CONTENT = ButtonContent.withShortcode("arrow_backward");
	private static final ButtonContent DEFAULT_NEXT_CONTENT = ButtonContent.withShortcode("arrow_forward");
	private static final ButtonContent DEFAULT_LAST_CONTENT = ButtonContent.withShortcode("fast_forward");
	private static final ButtonContent DEFAULT_DELETE_CONTENT = ButtonContent.withShortcode("wastebasket");

	protected PaginatorSupplier<R> paginatorSupplier;

	protected ButtonContent firstContent = DEFAULT_FIRST_CONTENT;
	protected ButtonContent previousContent = DEFAULT_PREVIOUS_CONTENT;
	protected ButtonContent nextContent = DEFAULT_NEXT_CONTENT;
	protected ButtonContent lastContent = DEFAULT_LAST_CONTENT;
	protected ButtonContent deleteContent = DEFAULT_DELETE_CONTENT;

	protected boolean hasDeleteButton;

	public BasicPaginatorBuilder(@NotNull Components componentsService) {
		super(componentsService);
	}

	/**
	 * Sets the {@link PaginatorSupplier} for this paginator
	 * <br>This is what supplies the pages dynamically for this paginator
	 *
	 * @param paginatorSupplier The {@link PaginatorSupplier} for this paginator
	 * @return This builder for chaining convenience
	 */
	public T setPaginatorSupplier(@NotNull PaginatorSupplier<R> paginatorSupplier) {
		this.paginatorSupplier = paginatorSupplier;

		return (T) this;
	}

	/**
	 * Specifies whether this paginator should have a delete button
	 * <br>Note that this button <b><i>does</i></b> cleanup used components
	 *
	 * @param hasDeleteButton <code>true</code> if the delete button has to appear
	 * @return This builder for chaining convenience
	 */
	public T useDeleteButton(boolean hasDeleteButton) {
		this.hasDeleteButton = hasDeleteButton;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the first page
	 *
	 * @param firstContent The {@link ButtonContent} for this button
	 * @return This builder for chaining convenience
	 */
	public T setFirstContent(ButtonContent firstContent) {
		this.firstContent = firstContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the previous page
	 *
	 * @param previousContent The {@link ButtonContent} for this button
	 * @return This builder for chaining convenience
	 */
	public T setPreviousContent(ButtonContent previousContent) {
		this.previousContent = previousContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the next page
	 *
	 * @param nextContent The {@link ButtonContent} for this button
	 * @return This builder for chaining convenience
	 */
	public T setNextContent(ButtonContent nextContent) {
		this.nextContent = nextContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the last page
	 *
	 * @param lastContent The {@link ButtonContent} for this button
	 * @return This builder for chaining convenience
	 */
	public T setLastContent(ButtonContent lastContent) {
		this.lastContent = lastContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which deletes this paginator
	 *
	 * @param deleteContent The {@link ButtonContent} for this button
	 * @return This builder for chaining convenience
	 */
	public T setDeleteContent(ButtonContent deleteContent) {
		this.deleteContent = deleteContent;

		return (T) this;
	}
}
