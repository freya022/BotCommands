package com.freya02.botcommands.api.annotations;

/**
 * Specifies the append mode of the specified attribute, such as permissions or guild IDs.
 * <br>Depending on the append mode, the attributes could inherit the attributes of the parent class, or of a base set
 * <br>For example, if the class has attributes but the method's mode is on {@code SET} then only the method's attributes are retained
 * <br>Otherwise, the class's and the method's attributes are retained
 */
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
