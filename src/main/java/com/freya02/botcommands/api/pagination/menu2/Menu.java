package com.freya02.botcommands.api.pagination.menu2;

import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.pagination.menu.ButtonContent;
import com.freya02.botcommands.api.pagination.transformer.EntryTransformer;

import java.util.List;

public final class Menu<E> extends BasicMenu<E, Menu<E>> {
	Menu(long ownerId,
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
	     PaginatorSupplier supplier) {
		super(ownerId, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent,
				makePages(entries, transformer, rowPrefixSupplier, maxEntriesPerPage),
				supplier);
	}
}
