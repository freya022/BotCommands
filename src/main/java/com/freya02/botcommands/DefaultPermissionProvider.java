package com.freya02.botcommands;

import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides a default permission provider for slash commands, also tells what commands are to be included in what guild.
 */
public class DefaultPermissionProvider implements PermissionProvider {
	@Override
	public Collection<String> getGuildCommands(String guildId) {
		return Collections.emptyList();
	}

	@Override
	public Collection<CommandPrivilege> getPermissions(String commandName, String guildId) {
		return Collections.emptyList();
	}
}
