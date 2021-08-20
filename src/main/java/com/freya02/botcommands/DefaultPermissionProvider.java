package com.freya02.botcommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides a default permission provider for application commands, also tells what commands are to be included in what guild.
 * <br>
 * This enables all guild commands and does not provide permissions
 */
public class DefaultPermissionProvider implements PermissionProvider {
	@Override
	@NotNull
	public CommandList getGuildCommands(Guild guild) {
		return CommandList.all();
	}

	@Override
	@NotNull
	public Collection<CommandPrivilege> getPermissions(String commandName, Guild guild) {
		return Collections.emptyList();
	}
}
