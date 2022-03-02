package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.ApplicationCommandInfoMapView;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface ApplicationCommandsContext {
	/**
	 * Returns the {@link SlashCommandInfo} object of the specified full slash command name, in the specific guild
	 *
	 * @param guild The Guild the command has been invoked in, can be null for global commands
	 * @param path Full name of the slash command (Examples: ban ; info/user ; ban/user/perm)
	 * @return The {@link SlashCommandInfo} object of the slash command
	 */
	@Nullable
	SlashCommandInfo findLiveSlashCommand(@NotNull Guild guild, @NotNull CommandPath path);

	/**
	 * Returns the {@link UserCommandInfo} object of the specified user context command name, in the specific guild
	 *
	 * @param guild The Guild the command has been invoked in, can be null for global commands
	 * @param name Name of the user context command
	 * @return The {@link UserCommandInfo} object of the user context command
	 */
	@Nullable
	UserCommandInfo findLiveUserCommand(@NotNull Guild guild, @NotNull String name);

	/**
	 * Returns the {@link MessageCommandInfo} object of the specified message context command name, in the specific guild
	 *
	 * @param guild The Guild the command has been invoked in, can be null for global commands
	 * @param name Name of the message context command
	 * @return The {@link MessageCommandInfo} object of the message context command
	 */
	@Nullable
	MessageCommandInfo findLiveMessageCommand(@NotNull Guild guild, @NotNull String name);

	/**
	 * Returns a view for all the registered application commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the application commands
	 */
	@NotNull
	@UnmodifiableView
	ApplicationCommandInfoMapView getApplicationCommandInfoMapView();

	/**
	 * Returns a view for all the registered slash commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the slash commands
	 */
	@NotNull
	@UnmodifiableView
	CommandInfoMap<SlashCommandInfo> getSlashCommandsMapView();

	/**
	 * Returns a view for all the registered user context commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the user context commands
	 */
	@NotNull
	@UnmodifiableView
	CommandInfoMap<UserCommandInfo> getUserCommandsMapView();

	/**
	 * Returns a view for all the registered message context commands
	 * <br>This doesn't filter commands on a per-guild basis
	 *
	 * @return A view of all the message context commands
	 */
	@NotNull
	@UnmodifiableView
	CommandInfoMap<MessageCommandInfo> getMessageCommandsMapView();

	/**
	 * Returns a list of the application commands paths, names such as <code>ban/user/perm</code>
	 *
	 * @return A list of the application commands paths
	 */
	List<CommandPath> getSlashCommandsPaths();

	/**
	 * Returns the live application commands for the specific guild
	 *
	 * @param guild The guild in which to query the commands, can be <code>null</code> for global commands
	 * @return The {@link ApplicationCommandInfoMapView} of the specific guild
	 */
	@NotNull ApplicationCommandInfoMapView getLiveApplicationCommandsMap(@Nullable Guild guild);
}
