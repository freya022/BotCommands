package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.components.event.ButtonEvent;

/**
 * Callback called when an item has been chosen in a {@link ChoiceMenu}
 *
 * @param <E> Type of the entries
 */
public interface ChoiceCallback<E> {
	/**
	 * Runs the callback
	 *
	 * @param event The {@link ButtonEvent} from the interacting user
	 * @param entry The selected entry from the {@link ChoiceMenu} elements
	 */
	void accept(ButtonEvent event, E entry);
}
