package com.freya02.botcommands.api.application.slash.annotations;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Cooldown;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.internal.annotations.LowercaseDiscordNamePattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

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
 *         <li><code>/nick</code></li>
 *     </ul>
 *     </li>
 *
 *     <li>Multiple commands with the same base name but different subcommand names, examples:
 *     <ul>
 *         <li><code>/info user</code></li>
 *         <li><code>/info role</code></li>
 *         <li><code>/info channel</code></li>
 *     </ul>
 *     </li>
 *
 *     <li>Multiple subcommands with the same base name and base group but with different subcommand names, examples:
 *     <ul>
 *         <li><code>/info simple user</code></li>
 *         <li><code>/info simple role</code></li>
 *         <li><code>/info complete user</code></li>
 *         <li><code>/info complete role</code></li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * Supported parameters, needs to be annotated with {@link AppOption @AppOption} :
 * <ul>
 *     <li>{@linkplain String}</li>
 *
 *     <li>boolean</li>
 *     <li>long</li>
 *     <li>double</li>
 *
 *     <li>{@linkplain Emoji}</li>
 *
 *     <li>{@linkplain IMentionable}</li>
 *     <li>{@linkplain Role}</li>
 *     <li>{@linkplain User}</li>
 *     <li>{@linkplain Member}</li>
 *
 *     <li>{@linkplain Category}</li>
 *     <li>{@linkplain GuildChannel}</li>
 *     <li>{@linkplain TextChannel}</li>
 *     <li>{@linkplain ThreadChannel}</li>
 *     <li>{@linkplain VoiceChannel}</li>
 *     <li>{@linkplain NewsChannel}</li>
 *     <li>{@linkplain StageChannel}</li>
 * </ul>
 *
 * <h2>To test your command, specify this command's scope as {@link CommandScope#GUILD} in order to instantly update the command in your guilds, see {@linkplain JDA#updateCommands()}</h2>
 *
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#subcommands-and-subcommand-groups">Discord docs</a>
 * @see AppOption @AppOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JDASlashCommand {
	/**
	 * Specified the application command scope for this command
	 *
	 * @return Scope of the command
	 *
	 * @see CommandScope
	 */
	CommandScope scope() default CommandScope.GUILD;

	/**
	 * Specifies whether the application command is disabled by default, so that administrators can further configure the command
	 * <br><b>If this is used in coordination with {@link UserPermissions} then they will be cleared if this is default locked</b>,
	 * as to allow discord to lock the command for everyone, until an admin configures it.
	 * <br>This does NOT affect administrators.
	 *
	 * <p>For example, maybe you want a ban command to be usable by someone who has a certain role, but which doesn't have the {@link Permission#BAN_MEMBERS BAN_MEMBERS} permission,
	 * you would then default lock the command and let the admins of the guild configure it
	 *
	 * @return <code>true</code> if the command should be disabled by default
	 */
	boolean defaultLocked() default false;

	/**
	 * Primary name of the command, <b>must not contain any spaces and no upper cases</b>
	 * <br>This can be a localization property
	 *
	 * @return Name of the command
	 */
	@LowercaseDiscordNamePattern
	String name();

	/**
	 * Command group of this command, <b>must not contain any spaces and no upper cases</b>
	 * <br>This can be a localization property
	 *
	 * @return Command group of the command
	 */
	@LowercaseDiscordNamePattern
	String group() default "";

	/**
	 * Subcommand name of this command, <b>must not contain any spaces and no upper cases</b>
	 * <br>This can be a localization property
	 *
	 * @return The subcommand name of this command
	 */
	@LowercaseDiscordNamePattern
	String subcommand() default "";

	/**
	 * Short description of the command, it is displayed in Discord
	 * <br>This can be a localization property
	 *
	 * @return Short description of the command
	 */
	String description() default "No description";
}
