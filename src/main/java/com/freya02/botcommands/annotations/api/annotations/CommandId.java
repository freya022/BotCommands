package com.freya02.botcommands.annotations.api.annotations;

import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.GuildApplicationSettings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows you to set a command ID to your application command methods
 *
 * See {@link GuildApplicationSettings#getGuildsForCommandId(String, CommandPath)} for more details
 *
 * @see GuildApplicationSettings#getGuildsForCommandId(String, CommandPath)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandId {
	/**
	 * @return The ID of this command method, must be unique
	 */
	String value();
}
