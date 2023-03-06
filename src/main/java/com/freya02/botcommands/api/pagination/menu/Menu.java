package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.pagination.paginator.Paginator;
import com.freya02.botcommands.api.pagination.transformer.EntryTransformer;
import com.freya02.botcommands.api.utils.ButtonContent;

import java.util.List;

/**
 * Paginator where pages are made from a list of entries.
 *
 * @param <E> Type of the entries
 * @see Paginator
 * @see ChoiceMenu
 */
public final class Menu<E> extends BasicMenu<E, Menu<E>> {
	Menu(InteractionConstraints constraints,
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
		super(constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent,
				makePages(entries, transformer, rowPrefixSupplier, maxEntriesPerPage),
				supplier);
	}
}
