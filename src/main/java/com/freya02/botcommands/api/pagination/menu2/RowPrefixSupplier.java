package com.freya02.botcommands.api.pagination.menu2;

public interface RowPrefixSupplier {
	String apply(int entryNum, int maxEntries);
}
