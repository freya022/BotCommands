package io.github.freya022.botcommands.api.pagination.paginator;

import io.github.freya022.botcommands.api.components.Components;
import org.jetbrains.annotations.NotNull;

/**
 * Builds a {@link Paginator}
 */
public final class PaginatorBuilder extends BasicPaginatorBuilder<PaginatorBuilder, Paginator> {
	private int maxPages;

	public PaginatorBuilder(@NotNull Components componentsService) {
		super(componentsService);
	}

	public PaginatorBuilder setMaxPages(int maxPages) {
		this.maxPages = maxPages;

		return this;
	}

	@Override
	@NotNull
	public Paginator build() {
		return new Paginator(componentsService, constraints, timeout, maxPages, paginatorSupplier, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);
	}
}
