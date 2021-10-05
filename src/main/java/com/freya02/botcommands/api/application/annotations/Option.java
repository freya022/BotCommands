package com.freya02.botcommands.api.application.annotations;

import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to set name and description of {@linkplain JdaSlashCommand application commands}
 * <p>
 * {@linkplain #name()} is optional if parameter name is available (add -parameters to your java compiler)
 *
 * <br><b>This needs to be used for context parameters (in case of User or Message) and component parameters too</b>, of course name and description is ignored in that case
 * @see Optional Optional (can also see @Nullable)
 * @see Nullable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
//TODO move package
public @interface Option {
	/**
	 * Name of the option, must follow the Discord specifications, see {@linkplain OptionData#OptionData(OptionType, String, String)} for details
	 * <p>
	 * <br>This is optional if:
	 * <ul>
	 *     <li>The parameter name is found (using -parameters on javac)</li>
	 *     <li>The parameter is not a slash command parameter</li>
	 * </ul>
	 * <br>This can be a localization property
	 *
	 * @return Name of the option
	 */
	String name() default "";

	/**
	 * Description of the option, must follow the Discord specifications, see {@linkplain OptionData#OptionData(OptionType, String, String)} for details
	 * <p>
	 * <br>This is optional if the parameter is not a slash command parameter, <b>otherwise it is defaulted to <code>"No Description"</code></b>
	 * <br>This can be a localization property
	 *
	 * @return Description of the option
	 */
	String description() default "";
}
