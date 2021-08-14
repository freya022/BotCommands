package com.freya02.botcommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Provides an interface to define {@linkplain CommandPrivilege command privileges} and what commands should be in which guilds
 */
public interface PermissionProvider {
	/**
	 * Returns the list of guild commands usable in that Guild
	 * <br><i>You can have a list of command names if needed in {@link BContext#getSlashCommandsPaths()}</i>
	 *
	 * @param guild Guild from which the commands should be
	 * @return A CommandList of this guild's commands
	 * @see CommandList#all()
	 * @see CommandList#none()
	 * @see CommandList#of(Collection)
	 * @see CommandList#notOf(Collection)
	 */
	@NotNull
	CommandList getGuildCommands(Guild guild);

	/**
	 * Returns the list of {@linkplain CommandPrivilege command privileges} for the given <b>base command name (most left name), no group, no subcommand</b>
	 *
	 * @param commandName Name of the command to get the permissions of
	 * @param guild       The guild of the command
	 * @return An empty Collection if the permissions should be cleared, or the privileges to apply to it.
	 */
	@NotNull
	Collection<CommandPrivilege> getPermissions(String commandName, Guild guild);
}