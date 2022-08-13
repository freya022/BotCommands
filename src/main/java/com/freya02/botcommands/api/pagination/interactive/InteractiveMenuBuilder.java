package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import org.jetbrains.annotations.NotNull;

/**
 * Builds an {@link InteractiveMenu}
 */
public final class InteractiveMenuBuilder extends BasicInteractiveMenuBuilder<InteractiveMenuBuilder, InteractiveMenu> {
	@Override
	@NotNull
	public InteractiveMenu build() {
		return new InteractiveMenu(constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent, items, usePaginator);
	}

	@Override
	public InteractiveMenuBuilder setPaginatorSupplier(@NotNull PaginatorSupplier<InteractiveMenu> paginatorSupplier) {
		throw new IllegalStateException("Interactive menu builder cannot have a PaginatorSupplier");
	}
}
