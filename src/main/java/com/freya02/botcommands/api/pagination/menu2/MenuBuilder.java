package com.freya02.botcommands.api.pagination.menu2;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MenuBuilder<E> extends BasicMenuBuilder<E, MenuBuilder<E>, Menu<E>> {
	public MenuBuilder(List<E> entries) {
		super(entries);
	}

	//TODO check docs, remove consumers and functions to replace them with specialised interfaces
	@Override
	@NotNull
	public Menu<E> build() {
		return new Menu<>(ownerId, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent, entries, maxEntriesPerPage, transformer, rowPrefixSupplier, paginatorSupplier);
	}
}
