package com.freya02.botcommands.slash.annotations;

import java.lang.annotation.*;

/**
 * One of the choices in a list of {@linkplain Choices}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Repeatable(value = Choices.class)
public @interface Choice {
	/**
	 * The name of the choice, this is what is shown on the Discord client
	 *
	 * @return The name of the choice
	 */
	String name();

	/**
	 * The string value of this Choice, only works for <code>String</code> parameters, it is not shown on Discord
	 * <br>You will receive the value on command execution
	 *
	 * @return The value of this choice
	 */
	String value() default "";

	/**
	 * The integer value of this Choice, only works for <code>long</code> parameters, it is not shown on Discord
	 * <br>You will receive the value on command execution
	 *
	 * @return The <code>int</code> value of this choice
	 */
	int intValue() default 0;

	/**
	 * The double value of this Choice, only works for <code>double</code> parameters, it is not shown on Discord
	 * <br>You will receive the value on command execution
	 *
	 * @return The <code>double</code> value of this choice
	 */
	double doubleValue() default 0.0d;
}
