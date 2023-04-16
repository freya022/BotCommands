package com.freya02.botcommands.api.prefixed.annotations;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Cooldown;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.internal.annotations.LowercaseDiscordNamePattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for text commands, see all possible options.
 *
 * <p>
 * <b>Requirements:</b>
 * <ul>
 *     <li>The method must be in the {@link CommandsBuilder#addSearchPath(String) search path}</li>
 *     <li>First parameter must be {@link BaseCommandEvent}, or, {@link CommandEvent} only for fallback commands</li>
 * </ul>
 * <p>Supported parameters needs to be annotated with {@link TextOption @TextOption}</p>
 *
 * @see TextOption
 * @see Hidden
 * @see ID
 * @see BotPermissions
 * @see UserPermissions
 * @see Cooldown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JDATextCommand {
	/**
	 * Primary name of the command, <b>must not contain any spaces</b>
	 *
	 * @return Name of this command
	 */
	@LowercaseDiscordNamePattern
	String name();

	/**
	 * Group name of the command, <b>must not contain any spaces</b>
	 *
	 * @return Group name of this command
	 */
	@LowercaseDiscordNamePattern
	String group() default "";

	/**
	 * Subcommand name of the command, <b>must not contain any spaces</b>
	 *
	 * @return Subcommand name of this command
	 */
	@LowercaseDiscordNamePattern
	String subcommand() default "";

	/**
	 * Specifies the specific order the executable has to be loaded in (1 is the most important)
	 *
	 * @return The order of the method to be loaded in
	 */
	int order() default 0;

	/**
	 * Secondary <b>paths</b> of the command, <b>must not contain any spaces</b>, must follow the same format as slash commands such as {@code name group subcommand}
	 *
	 * @return Secondary paths of the command
	 */
	String[] aliases() default {};

	/**
	 * Short description of the command, it is displayed in the help command
	 *
	 * @return Short description of the command
	 */
	String description() default "";
}
