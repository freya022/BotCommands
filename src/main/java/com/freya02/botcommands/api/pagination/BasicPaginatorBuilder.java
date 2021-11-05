package com.freya02.botcommands.api.pagination;

import com.freya02.botcommands.api.pagination.menu.ButtonContent;
import com.freya02.botcommands.api.utils.EmojiUtils;
import net.dv8tion.jda.api.entities.Emoji;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public abstract class BasicPaginatorBuilder<T extends BasicPaginationBuilder<T, R>, R extends BasicPagination<R>> extends BasicPaginationBuilder<T, R> {
	private static final Emoji FIRST_EMOJI = EmojiUtils.resolveJDAEmoji(":rewind:");
	private static final Emoji PREVIOUS_EMOJI = EmojiUtils.resolveJDAEmoji(":arrow_backward:");
	private static final Emoji NEXT_EMOJI = EmojiUtils.resolveJDAEmoji(":arrow_forward:");
	private static final Emoji LAST_EMOJI = EmojiUtils.resolveJDAEmoji(":fast_forward:");
	private static final Emoji DELETE_EMOJI = EmojiUtils.resolveJDAEmoji(":wastebasket:");

	protected PaginatorSupplier paginatorSupplier;

	protected ButtonContent firstContent = ButtonContent.withEmoji(FIRST_EMOJI);
	protected ButtonContent previousContent = ButtonContent.withEmoji(PREVIOUS_EMOJI);
	protected ButtonContent nextContent = ButtonContent.withEmoji(NEXT_EMOJI);
	protected ButtonContent lastContent = ButtonContent.withEmoji(LAST_EMOJI);
	protected ButtonContent deleteContent = ButtonContent.withEmoji(DELETE_EMOJI);

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
