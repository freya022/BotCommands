package com.freya02.botcommands.api.pagination;

import com.freya02.botcommands.api.pagination.menu.ButtonContent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public abstract class BasicPaginatorBuilder<T extends BasicPaginationBuilder<T, R>, R extends BasicPagination<R>> extends BasicPaginationBuilder<T, R> {
	private static final ButtonContent DEFAULT_FIRST_CONTENT = ButtonContent.withShortcode("rewind");
	private static final ButtonContent DEFAULT_PREVIOUS_CONTENT = ButtonContent.withShortcode("arrow_backward");
	private static final ButtonContent DEFAULT_NEXT_CONTENT = ButtonContent.withShortcode("arrow_forward");
	private static final ButtonContent DEFAULT_LAST_CONTENT = ButtonContent.withShortcode("fast_forward");
	private static final ButtonContent DEFAULT_DELETE_CONTENT = ButtonContent.withShortcode("wastebasket");

	protected PaginatorSupplier paginatorSupplier;

	protected ButtonContent firstContent = DEFAULT_FIRST_CONTENT;
	protected ButtonContent previousContent = DEFAULT_PREVIOUS_CONTENT;
	protected ButtonContent nextContent = DEFAULT_NEXT_CONTENT;
	protected ButtonContent lastContent = DEFAULT_LAST_CONTENT;
	protected ButtonContent deleteContent = DEFAULT_DELETE_CONTENT;

	protected boolean hasDeleteButton;

	public T setPaginatorSupplier(@NotNull PaginatorSupplier paginatorSupplier) {
		this.paginatorSupplier = paginatorSupplier;

		return (T) this;
	}

	public T useDeleteButton(boolean hasDeleteButton) {
		this.hasDeleteButton = hasDeleteButton;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the first page
	 *
	 * @param firstContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 */
	public T setFirstContent(ButtonContent firstContent) {
		this.firstContent = firstContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the previous page
	 *
	 * @param previousContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 */
	public T setPreviousContent(ButtonContent previousContent) {
		this.previousContent = previousContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the next page
	 *
	 * @param nextContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 */
	public T setNextContent(ButtonContent nextContent) {
		this.nextContent = nextContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which goes to the last page
	 *
	 * @param lastContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 */
	public T setLastContent(ButtonContent lastContent) {
		this.lastContent = lastContent;

		return (T) this;
	}

	/**
	 * Sets the content for the button which deletes this paginator
	 *
	 * @param deleteContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 */
	public T setDeleteContent(ButtonContent deleteContent) {
		this.deleteContent = deleteContent;

		return (T) this;
	}
}
