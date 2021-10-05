package com.freya02.botcommands.api.prefixed.annotations;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Cooldown;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.prefixed.CommandEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for bot commands, see all possible options
 * <p>First parameter may be {@link CommandEvent} only for fallback commands</p>
 *
 * @see AddExecutableHelp
 * @see AddSubcommandHelp
 * @see ArgExample
 * @see ArgName
 * @see MethodOrder
 * @see Hidden
 * @see ID
 * @see BotPermissions
 * @see UserPermissions
 * @see Cooldown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JdaTextCommand {
	/**
	 * Primary name of the command, <b>must not contain any spaces</b>
	 *
	 * @return Name of the command
	 */
	String name();

	String group() default "";

	String subcommand() default "";

	int order() default 0;

	/**
	 * Secondary <b>paths</b> of the command, <b>must not contain any spaces</b>, must follow the same format as slash commands such as <code>name/group/subcommand</code>
	 *
	 * @return Secondary paths of the command
	 */
	String[] aliases() default {};

	/**
	 * Short description of the command, it is displayed in the help command
	 *
	 * @return Short description of the command
	 */
	String description() default "No description";

	/**
	 * Name of the category the command should be in
	 * <b>This is ignored in a subcommand</b>
	 *
	 * @return Name of the category
	 */
	String category() default "No category";
}
