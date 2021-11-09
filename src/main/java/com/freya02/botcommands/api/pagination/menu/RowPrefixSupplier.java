package com.freya02.botcommands.api.pagination.menu;

public interface RowPrefixSupplier {
	String apply(int entryNum, int maxEntries);
}
