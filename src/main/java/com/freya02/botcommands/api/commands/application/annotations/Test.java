package com.freya02.botcommands.api.commands.application.annotations;

import com.freya02.botcommands.api.annotations.AppendMode;
import com.freya02.botcommands.api.core.config.BApplicationConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define an application command as being test-only.
 * <br>This means this application command will only be pushed in guilds
 * defined by {@link BApplicationConfig#getTestGuildIds()} and {@link #guildIds()}.
 *
 * <p><b>Note:</b>This only works on annotated commands.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Test {
	/**
	 * Specifies the {@link Guild} IDs in which the command should try to be inserted in
	 *
	 * @return The {@link Guild} IDs in which the command should go
	 */
	long[] guildIds() default {};

	/**
	 * Specifies the append mode for these guild IDs
	 *
	 * @return The {@link AppendMode} of these guilds IDs
	 * @see AppendMode
	 */
	AppendMode mode() default AppendMode.ADD;
}
