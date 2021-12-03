package com.freya02.botcommands.api.pagination.paginator;

import org.jetbrains.annotations.NotNull;

/**
 * Builds a {@link Paginator}
 */
public final class PaginatorBuilder extends BasicPaginatorBuilder<PaginatorBuilder, Paginator> {
	private int maxPages;

	public PaginatorBuilder setMaxPages(int maxPages) {
		this.maxPages = maxPages;

		return this;
	}

	@Override
	@NotNull
	public Paginator build() {
		return new Paginator(constraints, timeout, maxPages, paginatorSupplier, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);
	}
}
