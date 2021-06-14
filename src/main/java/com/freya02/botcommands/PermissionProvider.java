package com.freya02.botcommands;

import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import java.util.Collection;

/**
 * Provides an interface to define {@linkplain CommandPrivilege command privileges} and what commands should be in which guilds
 */
public interface PermissionProvider {
	/**
	 * Returns the list of guild commands usable in that Guild
	 *
	 * @param guildId Guild ID from which the commands should be
	 * @return An empty Collection if all commands should be here, or the names of the commands that are usable <b>(only base command name, no group, no subcommand)</b>
	 */
	Collection<String> getGuildCommands(String guildId);

	/**
	 * Returns the list of {@linkplain CommandPrivilege command privileges} for the given command name <b>(only base command name, no group, no subcommand)</b>
	 *
	 * @param commandName Name of the command to get the permissions of
	 * @param guildId     ID of the guild of the command
	 * @return An empty Collection if the permissions should be cleared, or the privileges to apply to it.
	 */
	Collection<CommandPrivilege> getPermissions(String commandName, String guildId);
}