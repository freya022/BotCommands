package com.freya02.botcommands.api.commands.application.annotations;

import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager;
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a function as one which declares commands, you can make your application commands in this function
 * <br>The first argument needs to be a {@link GuildApplicationCommandManager} or a {@link GlobalApplicationCommandManager}
 * <p>
 * <b>Note:</b> The function may be called more than once, for example, if the bot needs to update its commands, or if it joins a guild.
 *
 * <p><b>Requirement:</b> The declaring class must be annotated with {@link Command}.
 *
 * @see Command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AppDeclaration {}