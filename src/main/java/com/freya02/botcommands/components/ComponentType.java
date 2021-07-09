package com.freya02.botcommands.components;

import org.jetbrains.annotations.Nullable;

public enum ComponentType {
	PERSISTENT_BUTTON(0),
	LAMBDA_BUTTON(1),
	PERSISTENT_SELECTION_MENU(2),
	LAMBDA_SELECTION_MENU(3);

	private final int key;

	ComponentType(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}

	@Nullable
	public static ComponentType fromKey(int key) {
		for (ComponentType value : values()) {
			if (value.key == key) {
				return value;
			}
		}

		return null;
	}
}
