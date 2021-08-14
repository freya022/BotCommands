package com.freya02.botcommands.slash;

import com.freya02.botcommands.SettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * Interface providing getters for settings commands stuff on a per-guild basis
 *
 * <h2>Implementation note:</h2>
 * These settings are looked first in {@link SlashCommand} and then again in {@link SettingsProvider}
 * <br>This provides the user either a clean enough look in SettingsProvider (no boilerplate in every SlashCommand) or an easy to use method in SlashCommand
 */
public interface GuildSlashSettings {
	/**
	 * Retrieves the choices available for this command path
	 * <br>A command path is the complete name, a slash command displayed as <code>/name group subcommand</code> on Discord would be translated into <code>name/group/subcommand</code>
	 *
	 * @param guild       The Guild in which the command is, each guild will have their own choices, <code>null</code> for a global command
	 * @param cmdPath     The path of the command such as <code>name/group/subcommand</code>
	 * @param optionName  The <b>Discord name of the option</b> (such as <code>delete_days</code>), do not confuse with the parameter name of your method, even if it might be it
	 * @param optionIndex The index of the option name, should start from 1 (as parameter 0 is the event)
	 * @return A collection of choices for the specified command path in the specified {@link Guild}, the list can be empty or null to add no choices
	 * @see GuildSlashSettings GuildSlashSettings for implementation notes
	 */
	@Nullable
	default Collection<Command.Choice> getCommandChoices(@Nullable Guild guild, @Nonnull String cmdPath, @Nonnull String optionName, int optionIndex) {
		return Collections.emptyList();
	}

	/**
	 * Retrieves the option name for a command option
	 * <br>A command path is the complete name, a slash command displayed as <code>/name group subcommand</code> on Discord would be translated into <code>name/group/subcommand</code>
	 *
	 * @param guild       The Guild in which the command is, each guild will have their own choices, <code>null</code> for a global command
	 * @param cmdPath     The path of the command such as <code>name/group/subcommand</code>
	 * @param optionIndex The index of the option name, should start from 1 (as parameter 0 is the event)
	 * @return A custom option name for the specified command path in the specified {@link Guild}, the option name can be null or blank to not use a custom name
	 * @see GuildSlashSettings GuildSlashSettings for implementation notes
	 */
	@Nullable
	default String getOptionName(@Nullable Guild guild, @Nonnull String cmdPath, int optionIndex) {
		return null;
	}

	default String getCommandDescription(@Nullable Guild guild, @Nonnull String path) {
		return null;
	}

	default String getCommandName(@Nullable Guild guild, @Nonnull String path) {
		return null;
	}
}
