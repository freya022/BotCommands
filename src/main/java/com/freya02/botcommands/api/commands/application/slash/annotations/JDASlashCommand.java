package com.freya02.botcommands.api.commands.application.slash.annotations;

import com.freya02.botcommands.api.commands.annotations.BotPermissions;
import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.annotations.Cooldown;
import com.freya02.botcommands.api.commands.annotations.UserPermissions;
import com.freya02.botcommands.api.commands.application.AbstractApplicationCommandManager;
import com.freya02.botcommands.api.commands.application.CommandScope;
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder;
import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder;
import com.freya02.botcommands.api.core.config.BApplicationConfigBuilder;
import com.freya02.botcommands.api.core.options.annotations.Aggregate;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.internal.annotations.LowercaseDiscordNamePattern;
import kotlin.jvm.functions.Function1;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for slash commands, see all possible options
 *
 * <p>
 * Discord requires you to either have:
 * <ul>
 *     <li>1 unique command name, examples:
 *     <ul>
 *         <li>{@code /nick}</li>
 *     </ul>
 *     </li>
 *
 *     <li>Multiple commands with the same base name but different subcommand names, examples:
 *     <ul>
 *         <li>{@code /info user}</li>
 *         <li>{@code /info role}</li>
 *         <li>{@code /info channel}</li>
 *     </ul>
 *     </li>
 *
 *     <li>Multiple subcommands with the same base name and base group but with different subcommand names, examples:
 *     <ul>
 *         <li>{@code /info simple user}</li>
 *         <li>{@code /info simple role}</li>
 *         <li>{@code /info complete user}</li>
 *         <li>{@code /info complete role}</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * Input options needs to be annotated with {@link AppOption @AppOption}, see supported types at {@link ParameterResolver}
 *
 * <p><b>Requirement:</b> The declaring class must be annotated with {@link Command}.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#subcommands-and-subcommand-groups">Discord docs</a>
 * @see Command
 * @see AppOption @AppOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 * @see Aggregate
 *
 * @see AppDeclaration Declaring application commands using the DSL
 * @see AbstractApplicationCommandManager#slashCommand(String, CommandScope, KFunction, Function1) DSL equivalent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JDASlashCommand {
	/**
	 * Specifies the application command scope for this command
	 *
	 * <p><b>Default:</b> {@link CommandScope#GLOBAL_NO_DM}
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
	 * <p>For example, maybe you want a ban command to be usable by someone who has a certain role,
	 * but which doesn't have the {@link Permission#BAN_MEMBERS BAN_MEMBERS} permission,
	 * you would then default lock the command and let the admins of the guild configure it
	 *
	 * <p><b>Default:</b> false
	 *
	 * @return {@code true} if the command should be disabled by default
	 *
	 * @see TopLevelSlashCommandBuilder#setDefaultLocked(boolean) DSL equivalent
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
	 * @see TopLevelSlashCommandBuilder#setNsfw(boolean) DSL equivalent
	 */
	boolean nsfw() default false;

	/**
	 * Primary name of the command, <b>must not contain any spaces and no upper cases</b>.
	 *
	 * <p>
	 * This can be localized, see {@link LocalizationFunction} on how commands are mapped.
	 *
	 * @return Name of the command
	 *
	 * @see LocalizationFunction
	 */
	@LowercaseDiscordNamePattern
	String name();

	/**
	 * Command group of this command, <b>must not contain any spaces and no upper cases</b>.
	 *
	 * <p>
	 * This can be localized, see {@link LocalizationFunction} on how commands are mapped.
	 *
	 * @return Command group of the command
	 *
	 * @see LocalizationFunction
	 *
	 * @see TopLevelSlashCommandBuilder#subcommandGroup(String, Function1) DSL equivalent
	 */
	@LowercaseDiscordNamePattern
	String group() default "";

	/**
	 * Subcommand name of this command, <b>must not contain any spaces and no upper cases</b>.
	 *
	 * <p>
	 * This can be localized, see {@link LocalizationFunction} on how commands are mapped.
	 *
	 * @return The subcommand name of this command
	 *
	 * @see LocalizationFunction
	 *
	 * @see TopLevelSlashCommandBuilder#subcommand(String, KFunction, Function1) DSL equivalent (top level)
	 * @see SlashSubcommandGroupBuilder#subcommand(String, KFunction, Function1) DSL equivalent (in a subcommand group)
	 */
	@LowercaseDiscordNamePattern
	String subcommand() default "";

	/**
	 * Short description of the command, it is displayed in Discord.
	 *
	 * <p>
	 * If this description is omitted, a default localization is
	 * searched in {@link BApplicationConfigBuilder#addLocalizations(String, DiscordLocale...) the command localization bundles}
	 * using the root locale, for example: {@code MyCommands.json}.
	 *
	 * <p>
	 * This can be localized, see {@link LocalizationFunction} on how commands are mapped, example: {@code ban.description}.
	 *
	 * @return Short description of the command
	 *
	 * @see LocalizationFunction
	 *
	 * @see TopLevelSlashCommandBuilder#setDescription(String) DSL equivalent
	 */
	String description() default SlashCommandBuilder.DEFAULT_DESCRIPTION;
}
