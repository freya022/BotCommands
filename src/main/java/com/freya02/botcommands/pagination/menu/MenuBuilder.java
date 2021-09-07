package com.freya02.botcommands.pagination.menu;

import com.freya02.botcommands.pagination.Paginator;

import java.util.List;

/**
 * Provides a builder for {@link Menu}s
 *
 * @param <T> Type of the entries
 */
public class MenuBuilder<T> extends BaseMenu<T, MenuBuilder<T>> {
	/**
	 * Creates a new {@link Menu} builder
	 *
	 * @param userId       The ID of the only User who should be able to use this menu
	 *                     <br>An ID of 0 means this menu will be usable by everyone
	 * @param deleteButton Whether there should be a delete button on the {@link Paginator}
	 * @param entries      The entries which should be displayed to the user
	 */
	public MenuBuilder(long userId, boolean deleteButton, List<T> entries) {
		super(userId, deleteButton, entries);
	}

	public Menu build() {
		makePages();

		final Menu menu = new Menu(userId, pages.size(), deleteButton);

		menu.setMenuSupplier((builder, components, page) -> {
			final MenuPage<T> menuPage = pages.get(page);

			builder.setDescription(menuPage.getDescription());

			if (paginationSupplier != null) {
				paginationSupplier.accept(builder, components, page);
			}
		});

		return menu;
	}
}