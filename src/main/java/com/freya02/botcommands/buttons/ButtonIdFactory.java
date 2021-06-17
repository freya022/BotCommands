package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;

import java.util.Objects;

import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

/**
 * Helper class to create button IDs, primarily makes it so you don't need to type the handler name and whether the button is only usable once
 */
public class ButtonIdFactory {
	private static BContextImpl context;

	private final String handlerName;
	private final boolean oneUse;

	private boolean checked = false;

	public ButtonIdFactory(String handlerName, boolean oneUse) {
		this.handlerName = handlerName;
		this.oneUse = oneUse;
	}

	static void setContext(BContextImpl context) {
		ButtonIdFactory.context = context;
	}

	/**
	 * Creates a button with the previously supplied parameters
	 *
	 * @param args The arguments to pass to the handler
	 * @return A new button ID
	 */
	public String of(Object... args) {
		return ofUser(0L, args);
	}

	/**
	 * Creates a button with the previously supplied parameters
	 *
	 * @param args     The arguments to pass to the handler
	 * @param callerId The only allowed user ID for this button
	 * @return A new button ID
	 */
	public String ofUser(long callerId, Object... args) {
		if (!checked) {
			if (!buttonsMap.containsKey(handlerName))
				throw new IllegalStateException("Button listener with name '" + handlerName + "' doesn't exist");
			checked = true;
		}

		final String constructedId = ButtonId.constructId(handlerName, oneUse, callerId, args);

		return Objects.requireNonNull(context.getIdManager(), "ID Manager should be set in order to use Discord components").newId(constructedId, false);
	}
}
