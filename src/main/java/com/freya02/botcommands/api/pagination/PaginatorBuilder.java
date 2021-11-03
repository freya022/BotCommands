package com.freya02.botcommands.api.pagination;

public class PaginatorBuilder extends BasicPaginatorBuilder<PaginatorBuilder, Paginator> {
	private int maxPages;
	private PaginatorSupplier supplier;

	public PaginatorBuilder setMaxPages(int maxPages) {
		this.maxPages = maxPages;

		return this;
	}

	public PaginatorBuilder setSupplier(PaginatorSupplier supplier) {
		this.supplier = supplier;

		return this;
	}

	@Override
	public Paginator build() {
		return new Paginator(ownerId, timeout, maxPages, supplier, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);
	}
}
