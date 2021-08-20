package com.freya02.botcommands.application.slash.annotations;

import com.freya02.botcommands.CooldownScope;
import com.freya02.botcommands.Emoji;
import com.freya02.botcommands.EmojiOrEmote;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for application commands, see all possible options
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
 * Supported parameters:
 * <ul>
 *     <li>{@linkplain String}</li>
 *
 *     <li>boolean</li>
 *     <li>long</li>
 *     <li>double</li>
 *
 *     <li>{@linkplain Emoji}</li>
 *     <li>{@linkplain Emote}</li>
 *     <li>{@linkplain EmojiOrEmote}</li>
 *
 *     <li>{@linkplain IMentionable}</li>
 *     <li>{@linkplain Role}</li>
 *     <li>{@linkplain User}</li>
 *     <li>{@linkplain Member}</li>
 *     <li>{@linkplain TextChannel}</li>
 * </ul>
 *
 * <h2>To test your command, specify this command as guild-only in order to instantly update the command in your guilds, see {@linkplain JDA#updateCommands()}</h2>
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#subcommands-and-subcommand-groups">Discord docs</a>
 * @see Option
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JdaSlashCommand {
	/**
	 * Whether this application command should only work in a {@linkplain Guild}
	 *
	 * @return <code>true</code> if the application command only works in a {@linkplain Guild}
	 */
	boolean guildOnly() default true;

	/**
	 * Primary name of the command, <b>must not contain any spaces and no upper cases</b>
	 *
	 * @return Name of the command
	 */
	String name();

	/**
	 * Command group of this command, <b>must not contain any spaces and no upper cases</b>
	 *
	 * @return Command group of the command
	 */
	String group() default "";

	/**
	 * Subcommand name of this command, <b>must not contain any spaces and no upper cases</b>
	 *
	 * @return The subcommand name of this command
	 */
	String subcommand() default "";

	/**
	 * Short description of the command, it is displayed in Discord
	 *
	 * @return Short description of the command
	 */
	String description() default "No description";

	/**
	 * Required {@linkplain Permission permissions} of the bot
	 *
	 * @return Required {@linkplain Permission permissions} of the bot
	 */
	Permission[] botPermissions() default {};

	/**
	 * Required {@linkplain Permission permissions} of the user
	 *
	 * @return Required {@linkplain Permission permissions} of the user
	 */
	Permission[] userPermissions() default {};

	/**
	 * Cooldown time <b>in milliseconds</b> before the command can be used again in the scope specified by {@linkplain #cooldownScope()}
	 *
	 * @return Cooldown time
	 */
	int cooldown() default 0;

	/**
	 * Scope of the cooldown, can be either {@linkplain CooldownScope#USER}, {@linkplain CooldownScope#CHANNEL} or {@linkplain CooldownScope#GUILD}
	 *
	 * @return Scope of the cooldown
	 */
	CooldownScope cooldownScope() default CooldownScope.USER;
}
