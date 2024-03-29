package com.freya02.botcommands.api.application.slash.annotations;

import com.freya02.botcommands.api.application.annotations.AppOption;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows setting minimum and maximum string length on the specified {@link AppOption}.
 * <br><b>This is only for string types !</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Length {
	/**
	 * @return The minimum value of this parameter (included)
	 */
	int min() default 1;

	/**
	 * @return The maximum value of this parameter  (included)
	 */
	int max() default OptionData.MAX_STRING_OPTION_LENGTH;
}
