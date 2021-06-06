package com.freya02.botcommands.buttons;

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
	 * @param content Content of the component
	 * @return A new <b>unique</b> component ID
	 */
	String newId(String content);
}
