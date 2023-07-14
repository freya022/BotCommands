package com.freya02.botcommands.api.commands.application.context.annotations;

import com.freya02.botcommands.api.commands.annotations.BotPermissions;
import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.annotations.Cooldown;
import com.freya02.botcommands.api.commands.annotations.UserPermissions;
import com.freya02.botcommands.api.commands.application.AbstractApplicationCommandManager;
import com.freya02.botcommands.api.commands.application.CommandScope;
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration;
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder;
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent;
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import kotlin.jvm.functions.Function1;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for user commands.
 *
 * <p>
 * The targeted method must have a {@link GlobalMessageEvent} or a {@link GuildMessageEvent},
 * with the only accepted {@link SlashOption option} being {@link Message},
 * which will be the <i>targeted</i> message
 *
 * <p><b>Requirement:</b> The declaring class must be annotated with {@link Command}.
 *
 * @see GlobalMessageEvent#getTarget()
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#user-commands">Discord docs</a>
 * @see Command
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 *
 * @see AppDeclaration Declaring application commands using the DSL
 * @see AbstractApplicationCommandManager#messageCommand(String, CommandScope, KFunction, Function1) DSL equivalent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JDAMessageCommand {
	/**
	 * Specified the application command scope for this command.
	 *
	 * <p><b>Default:</b> {@link CommandScope#GLOBAL_NO_DM GLOBAL_NO_DM}
	 *
	 * @return Scope of the command
	 *
	 * @see CommandScope
	 */
	CommandScope scope() default CommandScope.GLOBAL_NO_DM;

	/**
	 * Specifies whether the application command is disabled for everyone but administrators by default,
	 * so that administrators can further configure the command.
	 *
	 * <br><b>Note:</b> you cannot use this with {@link UserPermissions}.
	 *
	 * <p><b>Default:</b> false
	 *
	 * @return {@code true} if the command should be disabled by default
	 *
	 * @see MessageCommandBuilder#setDefaultLocked(boolean) DSL equivalent
	 */
	boolean defaultLocked() default false;

	/**
	 * Specifies whether the application command is usable in NSFW channels.
	 * <br>Note: NSFW commands need to be enabled by the user in order to appear in DMs
	 *
	 * <p><b>Default:</b> false
	 *
	 * @return {@code true} if the command should only be usable in NSFW channels
	 *
	 * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
	 *
	 * @see MessageCommandBuilder#setNsfw(boolean) DSL equivalent
	 */
	boolean nsfw() default false;

	/**
	 * Primary name of the command, which can contain spaces and upper cases.
	 *
	 * <p>
	 * This can be localized, see {@link LocalizationFunction} on how commands are mapped.
	 *
	 * @return Name of the command
	 *
	 * @see LocalizationFunction
	 */
	String name();
}
