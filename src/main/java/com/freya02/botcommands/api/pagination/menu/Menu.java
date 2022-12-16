package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.data.InteractionConstraints;
import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.pagination.paginator.Paginator;
import com.freya02.botcommands.api.pagination.transformer.EntryTransformer;
import com.freya02.botcommands.api.utils.ButtonContent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides a menu
 * <br>You provide the entries, it makes the pages for you
 *
 * @param <E> Type of the entries
 * @see Paginator
 * @see ChoiceMenu
 */
public final class Menu<E> extends BasicMenu<E, Menu<E>> {
	Menu(@NotNull Components componentsService,
		 InteractionConstraints constraints,
		 TimeoutInfo<Menu<E>> timeout,
		 boolean hasDeleteButton,
		 ButtonContent firstContent,
		 ButtonContent previousContent,
		 ButtonContent nextContent,
		 ButtonContent lastContent,
		 ButtonContent deleteContent,
		 List<E> entries,
		 int maxEntriesPerPage,
		 EntryTransformer<? super E> transformer,
		 RowPrefixSupplier rowPrefixSupplier,
		 PaginatorSupplier<Menu<E>> supplier) {
		super(componentsService, constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent,
				makePages(entries, transformer, rowPrefixSupplier, maxEntriesPerPage),
				supplier);
	}
}
