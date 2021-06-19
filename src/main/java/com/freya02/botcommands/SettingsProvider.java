package com.freya02.botcommands;

public interface SettingsProvider {
	/**
	 * Returns the settings of the specified Guild ID
	 *
	 * @param guildId The Guild ID from which to retrieve the settings from
	 * @return The Guild's settings
	 */
	BGuildSettings getSettings(long guildId);
}
