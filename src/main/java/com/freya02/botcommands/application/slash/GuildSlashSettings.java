package com.freya02.botcommands.application.slash;

import com.freya02.botcommands.SettingsProvider;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Interface providing getters for settings commands stuff on a per-guild basis
 *
 * <h2>Implementation note:</h2>
 * These settings are looked first in {@link SlashCommand} and then again in {@link SettingsProvider}
 * <br>This provides the user either a clean enough look in SettingsProvider (no boilerplate in every SlashCommand) or an easy to use method in SlashCommand
 */
public interface GuildSlashSettings {
	/**
	 * Retrieves the localized command data for a command <b>(slash command, context command...)</b>
	 * <br>A command path is the complete name, a application command displayed as <code>/name group subcommand</code> on Discord would be translated into <code>name/group/subcommand</code>
	 *
	 * @param guild       The Guild in which the command is, each guild will have their own command name, <code>null</code> for a global command
	 * @param cmdPath     The path <b>(Not localized)</b> of the command such as <code>name/group/subcommand</code>
	 * @param optionNames The option <b>(Not localized)</b> names that this command has, uppercase in method parameters are transformed into <code>_ + lowercase equivalent</code>
	 *                    <br><b>This is null outside of slash commands</b>   
	 * @return A custom application command data for the specified command path in the specified {@link Guild}, this can be null
	 * @see GuildSlashSettings GuildSlashSettings for implementation notes
	 */
	@Nullable
	default LocalizedSlashCommandData getLocalizedCommandData(@Nullable Guild guild, @Nonnull String cmdPath, @Nullable List<String> optionNames) {
		return new LocalizedSlashCommandData(null, null, null, Collections.emptyList());
	}
}
