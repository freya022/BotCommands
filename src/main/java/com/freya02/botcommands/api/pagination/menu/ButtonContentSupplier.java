package com.freya02.botcommands.api.pagination.menu;

/**
 * @param <T> Item type
 * @see java.util.function.BiFunction
 */
public interface ButtonContentSupplier<T> {
	/**
	 * Returns a {@link ButtonContent} based on the given item and the current page number of the paginator
	 *
	 * @param item The item bound to this button
	 * @param index The index of this item on the current page number of the paginator
	 * @return The {@link ButtonContent} of this item
	 */
	ButtonContent apply(T item, int index);
}
