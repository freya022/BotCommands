package com.freya02.botcommands.api.commands.application.annotations;

import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager;
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for functions which declares application commands.
 * <br>The first argument needs to be a {@link GuildApplicationCommandManager} or a {@link GlobalApplicationCommandManager}.
 * <p>
 * <b>Note:</b> The function may declare no command,
 * and may be called more than once, for example,
 * if the bot needs to update its commands, or if it joins a guild.
 *
 * <p><b>Requirement:</b> The declaring class must be annotated with {@link Command}.
 *
 * @see Command
 *
 * @see JDASlashCommand
 * @see JDAMessageCommand
 * @see JDAUserCommand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AppDeclaration {}