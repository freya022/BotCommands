package io.github.freya022.botcommands.api.pagination.menu;

public interface RowPrefixSupplier {
	String apply(int entryNum, int maxEntries);
}
