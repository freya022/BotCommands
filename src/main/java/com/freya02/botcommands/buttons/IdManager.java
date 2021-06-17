package com.freya02.botcommands.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.util.function.Consumer;

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
	 */
	void removeId(String buttonId, boolean isTemporary);

	/**
	 * Returns the ButtonClickEvent consumer for this handler ID
	 *
	 * @param handlerId Handler ID of the consumer
	 * @return A ButtonClickEvent consumer
	 */
	Consumer<ButtonClickEvent> getAction(int handlerId);

	/**
	 * Returns a new handler ID for the specified ButtonClickEvent consumer
	 *
	 * @param action The ButtonClickEvent consumer to be registered
	 * @return A new handler ID for this action
	 */
	int newHandlerId(Consumer<ButtonClickEvent> action);
}
