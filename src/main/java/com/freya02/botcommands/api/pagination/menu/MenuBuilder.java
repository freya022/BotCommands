package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.components.Components;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Builds a {@link Menu}
 *
 * @param <E> Type of the entries
 */
public final class MenuBuilder<E> extends BasicMenuBuilder<E, MenuBuilder<E>, Menu<E>> {
	public MenuBuilder(@NotNull Components componentsService, @NotNull List<E> entries) {
		super(componentsService, entries);
	}

	@Override
	@NotNull
	public Menu<E> build() {
		return new Menu<>(componentsService, constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent, entries, maxEntriesPerPage, transformer, rowPrefixSupplier, paginatorSupplier);
	}
}
