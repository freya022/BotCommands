package com.freya02.botcommands.api.pagination;

import com.freya02.botcommands.api.pagination.menu.ButtonContent;

public final class Paginator extends BasicPaginator<Paginator> {
	Paginator(long ownerId, TimeoutInfo<Paginator> timeout, int _maxPages, PaginatorSupplier supplier, boolean hasDeleteButton, ButtonContent firstContent, ButtonContent previousContent, ButtonContent nextContent, ButtonContent lastContent, ButtonContent deleteContent) {
		super(ownerId, timeout, _maxPages, supplier, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);
	}
}
