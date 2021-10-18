package com.freya02.botcommands.api.annotations;

public enum AppendMode {
	/**
	 * Adds the attributes of this method with the attributes of the class, if they exist,
	 * otherwise uses the attributes of the method
	 */
	ADD,

	/**
	 * Overrides the attributes of the class if they exist,
	 * otherwise uses the attributes of the method
	 */
	SET
}
