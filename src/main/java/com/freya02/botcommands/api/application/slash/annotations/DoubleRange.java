package com.freya02.botcommands.api.application.slash.annotations;

import com.freya02.botcommands.api.application.annotations.AppOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows setting minimum and maximum values on the specified {@link AppOption}
 * <br><b>This is only for floating point number types !</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DoubleRange {
	/**
	 * @return The minimum value of this parameter (included)
	 */
	double from();

	/**
	 * @return The maximum value of this parameter (included)
	 */
	double to();
}
