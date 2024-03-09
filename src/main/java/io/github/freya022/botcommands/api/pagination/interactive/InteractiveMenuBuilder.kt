package io.github.freya022.botcommands.api.pagination.interactive;

import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier;
import org.jetbrains.annotations.NotNull;

/**
 * Builds an {@link InteractiveMenu}
 */
public final class InteractiveMenuBuilder extends BasicInteractiveMenuBuilder<InteractiveMenuBuilder, InteractiveMenu> {
	public InteractiveMenuBuilder(@NotNull Components componentsService) {
		super(componentsService);
	}

	@Override
	@NotNull
	public InteractiveMenu build() {
		return new InteractiveMenu(componentsService, constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent, items, usePaginator);
	}

	@Override
	public InteractiveMenuBuilder setPaginatorSupplier(@NotNull PaginatorSupplier<InteractiveMenu> paginatorSupplier) {
		throw new IllegalStateException("Interactive menu builder cannot have a PaginatorSupplier");
	}
}
