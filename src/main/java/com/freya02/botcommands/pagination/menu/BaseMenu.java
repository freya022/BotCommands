package com.freya02.botcommands.pagination.menu;

import com.freya02.botcommands.pagination.PaginationSupplier;
import com.freya02.botcommands.pagination.transformer.EntryTransformer;
import com.freya02.botcommands.pagination.transformer.StringTransformer;
import com.freya02.botcommands.utils.BiIntFunction;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseMenu<T, R extends BaseMenu<T, R>> {
	protected final long userId;
	protected final boolean deleteButton;
	private final List<T> entries;

	protected final Map<Integer, MenuPage<T>> pages = new HashMap<>();
	protected PaginationSupplier paginationSupplier;

	private int maxEntriesPerPage = 5;
	protected EntryTransformer<? super T> transformer = new StringTransformer();

	private BiIntFunction<String> rowPrefixSupplier = (entryNum, maxEntry) -> {
		final int spaces = MenuBuilder.getPadding(entryNum, maxEntry);

		return "`" + " ".repeat(spaces) + entryNum + ".` ";
	};


	public BaseMenu(long userId, boolean deleteButton, List<T> entries) {
		this.userId = userId;
		this.deleteButton = deleteButton;
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
		final double entryDigits = Math.log10(entryNum) + 1;
		final double maxEntryDigits = Math.log10(maxEntry) + 1;
		return (int) Math.ceil(maxEntryDigits - entryDigits);
	}

	/**
	 * Sets the row prefix supplier for this menu
	 * <br>This is what gets printed before each entry when it gets displayed
	 *
	 * @param rowPrefixSupplier The row prefix supplier, the first parameter is the entry number, and the second parameter is the max entries number
	 * @return This builder for chaining convenience
	 */
	@SuppressWarnings("unchecked")
	public R setRowPrefix(BiIntFunction<String> rowPrefixSupplier) {
		this.rowPrefixSupplier = rowPrefixSupplier;

		return (R) this;
	}

	/**
	 * Sets the maximum number of entries per page<br>
	 * <b>This does not mean there will be X entries per page</b> but rather it will try to fit 5 entries maximum per page, if some text is too long it'll cut down the number of entries
	 *
	 * @param maxEntriesPerPage The maximum amount of entries per page
	 * @return This builder for chaining convenience
	 */
	@SuppressWarnings("unchecked")
	public R setMaxEntriesPerPage(int maxEntriesPerPage) {
		this.maxEntriesPerPage = maxEntriesPerPage;

		return (R) this;
	}

	/**
	 * Here the pagination supplier is more about adding further more stuff in the embed, or more components<br>
	 * <b>The embed should be almost full so be aware that it might not fit into Discord limits</b>
	 *
	 * @param paginationSupplier The optional {@linkplain PaginationSupplier}
	 * @return This builder for chaining convenience
	 */
	@SuppressWarnings("unchecked")
	public R setPaginationSupplier(PaginationSupplier paginationSupplier) {
		Checks.notNull(paginationSupplier, "Pagination supplier");

		this.paginationSupplier = paginationSupplier;

		return (R) this;
	}

	/**
	 * Sets the entry transformer for this menu
	 *
	 * @param transformer The {@link EntryTransformer} to use to stringify the entries
	 * @return This builder for chaining convenience
	 */
	@SuppressWarnings("unchecked")
	public R setTransformer(EntryTransformer<? super T> transformer) {
		Checks.notNull(transformer, "Entry transformer");

		this.transformer = transformer;

		return (R) this;
	}

	protected void makePages() {
		int page = 0;
		int oldEntry = 0;
		StringBuilder builder = new StringBuilder();

		for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
			T entry = entries.get(i);

			final String s = transformer.toString(entry);
			Checks.notLonger(s, MessageEmbed.TEXT_MAX_LENGTH - 8, "Entry #" + i + " string");

			if (i - oldEntry >= maxEntriesPerPage || builder.length() + s.length() > MessageEmbed.TEXT_MAX_LENGTH - 8) {
				pages.put(page, new MenuPage<>(builder.toString(), entries.subList(oldEntry, i)));

				page++;
				oldEntry = i;

				builder.setLength(0);
			}

			builder.append(rowPrefixSupplier.apply(i - oldEntry + 1, maxEntriesPerPage)).append(s).append('\n');
		}

		pages.put(page, new MenuPage<>(builder.toString(), entries.subList(oldEntry, entries.size())));
	}
}
