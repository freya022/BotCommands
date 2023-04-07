package com.freya02.botcommands.api.commands.application.annotations;

import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows you to set a command ID on your application command methods.
 *
 * @see ApplicationCommand#getGuildsForCommandId(String, CommandPath)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandId {
	/**
	 * @return The ID of this command method, must be unique
	 */
	String value();
}
