package com.freya02.botcommands.api.commands.application;

import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.core.service.annotations.InjectedService;
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@InjectedService
public interface ApplicationCommandsContext {
	/**
	 * Returns the {@link SlashCommandInfo} object of the specified full slash command name, in the specific guild
	 *
	 * @param guild The Guild the command has been invoked in, can be null for global commands
	 * @param path  Full name of the slash command (Examples: ban ; info/user ; ban/user/perm)
	 *
	 * @return The {@link SlashCommandInfo} object of the slash command
	 */
	@Nullable
	SlashCommandInfo findLiveSlashCommand(@Nullable Guild guild, @NotNull CommandPath path);

	/**
	 * Returns the {@link UserCommandInfo} object of the specified user context command name, in the specific guild
	 *
	 * @param guild The Guild the command has been invoked in, can be null for global commands
	 * @param name  Name of the user context command
	 *
	 * @return The {@link UserCommandInfo} object of the user context command
	 */
	@Nullable
	UserCommandInfo findLiveUserCommand(@Nullable Guild guild, @NotNull String name);

	/**
	 * Returns the {@link MessageCommandInfo} object of the specified message context command name, in the specific guild
	 *
	 * @param guild The Guild the command has been invoked in, can be null for global commands
	 * @param name  Name of the message context command
	 *
	 * @return The {@link MessageCommandInfo} object of the message context command
	 */
	@Nullable
	MessageCommandInfo findLiveMessageCommand(@Nullable Guild guild, @NotNull String name);

	/**
	 * Returns the live application commands for the specific guild
	 *
	 * @param guild The guild in which to query the commands, can be <code>null</code> for global commands
	 *
	 * @return The {@link ApplicationCommandMap} of the specific guild
	 */
	@NotNull ApplicationCommandMap getLiveApplicationCommandsMap(@Nullable Guild guild);

	/**
	 * Returns the effective application commands available for the specific guild.
	 *
	 * @param guild The guild in which to query the commands, can be {@code null} for global commands
	 *
	 * @return The {@link ApplicationCommandMap} of the specific guild
	 */
	@NotNull ApplicationCommandMap getEffectiveApplicationCommandsMap(@Nullable Guild guild);

	/**
	 * Updates the application commands for the global scope <br><br>
	 *
	 * @param force Whether the commands should be updated no matter what
	 *
	 * @return A {@link CompletableFuture CompletableFuture}&lt;{@link CommandUpdateResult}&gt;
	 */
	@NotNull
	CompletableFuture<CommandUpdateResult> updateGlobalApplicationCommands(boolean force);

	/**
	 * Updates the application commands in the specified guild
	 *
	 * <p>Why you could call this method:
	 * <ul>
	 *     <li>Your bot joins a server and you wish to add a guild command to it </li>
	 *     <li>You decide to remove a command from a guild while the bot is running, <b>I do not mean code hotswap! It will not work that way</b></li>
	 * </ul>
	 *
	 * @param guild The guild which needs to be updated
	 * @param force Whether the commands should be updated no matter what
	 *
	 * @return A {@link CompletableFuture CompletableFuture}&lt;{@link CommandUpdateResult}&gt;
	 */
	@NotNull
	CompletableFuture<CommandUpdateResult> updateGuildApplicationCommands(@NotNull Guild guild, boolean force);
}
