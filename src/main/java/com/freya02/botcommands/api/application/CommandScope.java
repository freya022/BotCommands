package com.freya02.botcommands.api.application;

import com.freya02.botcommands.annotations.api.application.annotations.Test;
import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.builder.ApplicationCommandsBuilder;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Defines command scopes for application commands
 * <br>See the constant docs
 */
public enum CommandScope {
	/**
	 * The guild command scope, only pushes application commands to the guilds
	 * <br>Can be filtered with {@link ApplicationCommand#getGuildsForCommandId(BContext, String, CommandPath)} and {@link SettingsProvider#getGuildCommands(Guild)}
	 * <br>Can be forced with {@link ApplicationCommandsBuilder#forceCommandsAsGuildOnly(boolean)} and {@link Test}
	 */
	GUILD(true),
	/**
	 * The global command scope, pushes this command to the first shard
	 *
	 * <p>Cannot be filtered on a per-guild basis
	 */
	GLOBAL(false),
	/**
	 * The global command scope, but with DMs disabled, pushes this command to the first shard
	 * <br>This might be useful to have guild commands but without having to push them on every guild
	 *
	 * <p>Cannot be filtered on a per-guild basis
	 */
	GLOBAL_NO_DM(true);

	private final boolean guildOnly;

	CommandScope(boolean guildOnly) {this.guildOnly = guildOnly;}

	public boolean isGuildOnly() {
		return guildOnly;
	}
}
