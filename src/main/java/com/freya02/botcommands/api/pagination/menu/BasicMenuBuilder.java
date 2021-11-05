package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.pagination.paginator.BasicPaginatorBuilder;
import com.freya02.botcommands.api.pagination.transformer.EntryTransformer;
import com.freya02.botcommands.api.pagination.transformer.StringTransformer;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides base for a menu builder
 *
 * @param <E> Type of the menu entries
 * @param <T> Type of the implementor
 * @param <R> Type of the implementor {@link #build()} return type
 */
@SuppressWarnings("unchecked")
public abstract class BasicMenuBuilder<E, T extends BasicMenuBuilder<E, T, R>, R extends BasicMenu<E, R>> extends BasicPaginatorBuilder<T, R> {
	protected final List<E> entries;

	protected int maxEntriesPerPage = 5;

	protected EntryTransformer<? super E> transformer = new StringTransformer();

	protected RowPrefixSupplier rowPrefixSupplier = (entryNum, maxEntry) -> {
		final int spaces = MenuBuilder.getPadding(entryNum, maxEntry);

		return "`" + " ".repeat(spaces) + entryNum + ".` ";
	};

	protected BasicMenuBuilder(@NotNull List<E> entries) {
		this.entries = entries;
	}

	/**
	 * Returns the padding needed between this entry number and the maximum entry number
	 *
	 * @param entryNum The current entry number
	 * @param maxEntry The maximum entry number
	 * @return The number of padding spaces needed
	 */
	public static int getPadding(int entryNum, int maxEntry) {
		final double entryDigits = Math.floor(Math.log10(entryNum) + 1);
		final double maxEntryDigits = Math.floor(Math.log10(maxEntry) + 1);
		return (int) (maxEntryDigits - entryDigits);
	}

	/**
	 * Sets the maximum number of entries per page<br>
	 * <b>This does not mean there will be X entries per page</b> but rather it will try to fit 5 entries maximum per page, if some text is too long it'll cut down the number of entries
	 *
	 * @param maxEntriesPerPage The maximum amount of entries per page
	 * @return This builder for chaining convenience
	 */
	public T setMaxEntriesPerPage(int maxEntriesPerPage) {
		Checks.positive(maxEntriesPerPage, "Max entries per page");

		this.maxEntriesPerPage = maxEntriesPerPage;

		return (T) this;
	}

	/**
	 * Sets the row prefix supplier for this menu
	 * <br>This is what gets printed before each entry when it gets displayed
	 *
	 * @param rowPrefixSupplier The row prefix supplier, the first parameter is the entry number, and the second parameter is the max entries number
	 * @return This builder for chaining convenience
	 */
	public T setRowPrefixSupplier(@NotNull RowPrefixSupplier rowPrefixSupplier) {
		this.rowPrefixSupplier = rowPrefixSupplier;

		return (T) this;
	}

	/**
	 * Sets the entry transformer for this menu
	 *
	 * @param transformer The {@link EntryTransformer} to use to stringify the entries
	 * @return This builder for chaining convenience
	 */
	public T setTransformer(@NotNull EntryTransformer<? super E> transformer) {
		Checks.notNull(transformer, "Entry transformer");

		this.transformer = transformer;

		return (T) this;
	}
}