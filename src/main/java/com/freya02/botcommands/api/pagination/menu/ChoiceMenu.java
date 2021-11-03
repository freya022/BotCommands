package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.pagination.OldPaginationSupplier;
import com.freya02.botcommands.api.pagination.Paginator;

/**
 * An extension of Paginator, this takes a list of entries and generates the pages for you, which each entry taking a new line<br>
 * The user can then select an entry with the buttons, if you provide a callback then it'll be called when an entry is selected
 *
 * @see Paginator
 */
public class ChoiceMenu extends Paginator {
	ChoiceMenu(long userId, int size, boolean deleteButton) {
		super(userId, size, deleteButton);
	}

	/**
	 * {@inheritDoc} <br>
	 * <b>Throws <code>UnsupportedOperationException</code> as this pagination supplier is managed by the pagination.</b>
	 *
	 * @return
	 */
	@Override
	public Paginator setPaginationSupplier(OldPaginationSupplier paginationSupplier) {
		throw new UnsupportedOperationException();
	}

	void setMenuSupplier(OldPaginationSupplier paginationSupplier) {
		super.setPaginationSupplier(paginationSupplier);
	}
}
