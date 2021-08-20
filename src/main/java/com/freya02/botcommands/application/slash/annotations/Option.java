package com.freya02.botcommands.application.slash.annotations;

import com.freya02.botcommands.annotation.Optional;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to set name and description of {@linkplain JdaSlashCommand application commands}
 * <p>
 * {@linkplain #name()} is optional if parameter name is available (add -parameters to your java compiler)
 *
 * @see Optional
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Option {
	/**
	 * Name of the option, must follow the Discord specifications, see {@linkplain OptionData#OptionData(OptionType, String, String)} for details
	 * <p>
	 * <b>This is optional if parameter name is found</b>
	 *
	 * @return Name of the option
	 */
	String name() default "";

	/**
	 * Description of the option, must follow the Discord specifications, see {@linkplain OptionData#OptionData(OptionType, String, String)} for details
	 * <p>
	 * <b>This is optional and defaulted with <code>"No Description"</code></b>
	 *
	 * @return Description of the option
	 */
	String description() default "";
}
