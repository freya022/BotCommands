package com.freya02.botcommands.api.commands.application.slash.annotations;

import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

//TODO see if we can auto generate description based off a MessageFormat, could cause issues with localization
/**
 * Allows generating N command options from the specified {@link AppOption} or {@link TextOption}.
 * <br>The target parameter must be of type {@link List}.
 * <br>You can configure how many arguments are required with {@link #numRequired()}.
 * <br><b>Note:</b> you are limited to 1 vararg parameter in text commands
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface VarArgs {
	/**
	 * @return The number of times this option needs to appear, must be between 1 and 25 (max number of options)
	 */
	int value();

	/**
	 * @return The number of required options for this vararg
	 */
	int numRequired() default 1;
}
