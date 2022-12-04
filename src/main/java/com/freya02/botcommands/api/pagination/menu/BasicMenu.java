package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.new_components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.pagination.paginator.BasicPaginator;
import com.freya02.botcommands.api.pagination.transformer.EntryTransformer;
import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <E> Type of the menu elements
 * @param <T> Type of the implementor
 */
public abstract class BasicMenu<E, T extends BasicMenu<E, T>> extends BasicPaginator<T> {
	protected final Map<Integer, MenuPage<E>> pages;

	protected BasicMenu(InteractionConstraints constraints,
	                    TimeoutInfo<T> timeout,
	                    boolean hasDeleteButton,
	                    ButtonContent firstContent,
	                    ButtonContent previousContent,
	                    ButtonContent nextContent,
	                    ButtonContent lastContent,
	                    ButtonContent deleteContent,
	                    @NotNull Map<Integer, MenuPage<E>> pages,
	                    @Nullable PaginatorSupplier<T> supplier) {
		super(constraints, timeout, pages.size(), supplier, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);

		this.pages = pages;
	}

	@SuppressWarnings("unchecked")
	@Override
	@NotNull
	protected MessageEmbed getEmbed() {
		final EmbedBuilder builder;

		if (supplier != null) {
			builder = new EmbedBuilder(supplier.get((T) this, messageBuilder, components, page));
		} else {
			builder = new EmbedBuilder();
		}

		final MenuPage<E> menuPage = pages.get(page);

		builder.appendDescription(menuPage.content());

		return builder.build();
	}

	@NotNull
	protected static <E> Map<Integer, MenuPage<E>> makePages(@NotNull List<E> entries,
	                                                         @NotNull EntryTransformer<? super E> transformer,
	                                                         @NotNull RowPrefixSupplier rowPrefixSupplier,
	                                                         int maxEntriesPerPage) {
		final Map<Integer, MenuPage<E>> pages = new HashMap<>();

		int page = 0;
		int oldEntry = 0;
		StringBuilder builder = new StringBuilder();

		for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
			E entry = entries.get(i);

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

		return pages;
	}
}
