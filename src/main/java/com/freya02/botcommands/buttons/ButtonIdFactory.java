package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;

import java.util.Objects;

import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

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

	public String of(Object... args) {
		return ofUser(0L, args);
	}

	public String ofUser(long callerId, Object... args) {
		if (!checked) {
			if (!buttonsMap.containsKey(handlerName))
				throw new IllegalStateException("Button listener with name '" + handlerName + "' doesn't exist");
			checked = true;
		}

		final String constructedId = ButtonId.constructId(handlerName, oneUse, callerId, args);

		return Objects.requireNonNull(context.getIdManager(), "ID Manager should be set in order to use Discord components").newId(constructedId);
	}
}
