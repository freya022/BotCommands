package com.freya02.botcommands.api.annotations;

import com.freya02.botcommands.api.application.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify a command (text or application commands) parameter is supplied from discord, <i>i.e. it is <b>not</b> a custom parameter</i>
 * <br>This also can set name and description of {@linkplain JdaSlashCommand slash commands} parameters
 * <p>
 * {@linkplain #name()} is optional if the parameter name is available (add -parameters to your java compiler)
 *
 * @see Optional Optional (can also see @Nullable)
 * @see Nullable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
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
