package com.freya02.botcommands.buttons;

import java.util.Collection;

public interface IdManager {
	/**
	 * Returns the content of the given component ID
	 *
	 * @param buttonId ID of the component to get the content from
	 * @return Content of the component with the given ID
	 */
	String getContent(String buttonId);

	/**
	 * Creates a new component ID and associates it with the content
	 *
	 * @param content   Content of the component
	 * @param temporary <code>true</code> if the ID must be cleared at the next startup
	 * @return A new <b>unique</b> component ID
	 */
	String newId(String content, boolean temporary);

	/**
	 * Removes the given component ID from storage
	 *
	 * @param buttonId ID of the component to remove from the storage
	 * @param isTemporary If the component IDs are temporary (deleted on startup)
	 */
	void removeId(String buttonId, boolean isTemporary);

	/**
	 * Removes the given components IDs from storage<br>
	 * <b>Button IDs could be temporary or not (temporary = tied to a ButtonConsumer)</b><br>
	 * <b>Also removes ButtonConsumer if temporary</b>
	 *
	 * @param buttonIds ID of the components to remove from the storage
	 */
	void removeIds(Collection<String> buttonIds);

	/**
	 * Returns the ButtonClickEvent consumer for this handler ID
	 *
	 * @param handlerId Handler ID of the consumer
	 * @return A ButtonClickEvent consumer
	 */
	ButtonConsumer getAction(int handlerId);

	/**
	 * Returns a new handler ID for the specified ButtonClickEvent consumer
	 *
	 * @param action The ButtonClickEvent consumer to be registered
	 * @return A new handler ID for this action
	 */
	int newHandlerId(ButtonConsumer action);
}
