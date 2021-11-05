package com.freya02.botcommands.api.pagination;

import org.jetbrains.annotations.NotNull;

public final class PaginatorBuilder extends BasicPaginatorBuilder<PaginatorBuilder, Paginator> {
	private int maxPages;

	public PaginatorBuilder setMaxPages(int maxPages) {
		this.maxPages = maxPages;

		return this;
	}

	@Override
	@NotNull
	public Paginator build() {
		return new Paginator(ownerId, timeout, maxPages, paginatorSupplier, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);
	}
}
