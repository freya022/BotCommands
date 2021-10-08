package com.freya02.botcommands.api.application.slash.annotations;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Cooldown;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

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
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#subcommands-and-subcommand-groups">Discord docs</a>
 * @see AppOption @AppOption
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
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
	 * <br>This can be a localization property
	 *
	 * @return Name of the command
	 */
	String name();

	/**
	 * Command group of this command, <b>must not contain any spaces and no upper cases</b>
	 * <br>This can be a localization property
	 *
	 * @return Command group of the command
	 */
	String group() default "";

	/**
	 * Subcommand name of this command, <b>must not contain any spaces and no upper cases</b>
	 * <br>This can be a localization property
	 *
	 * @return The subcommand name of this command
	 */
	String subcommand() default "";

	/**
	 * Short description of the command, it is displayed in Discord
	 * <br>This can be a localization property
	 *
	 * @return Short description of the command
	 */
	String description() default "No description";
}
