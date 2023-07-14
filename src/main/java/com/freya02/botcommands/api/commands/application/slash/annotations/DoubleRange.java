package com.freya02.botcommands.api.commands.application.slash.annotations;

import com.freya02.botcommands.api.commands.application.ValueRange;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the minimum and maximum values on the specified {@link SlashOption}.
 * <br><b>Note:</b> this is only for floating point number types!
 *
 * @see SlashCommandOptionBuilder#setValueRange(ValueRange) DSL equivalent
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
