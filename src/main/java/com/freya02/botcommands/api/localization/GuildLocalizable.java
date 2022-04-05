package com.freya02.botcommands.api.localization;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Enables the subclass to localize strings with the current guild locale
 */
public interface GuildLocalizable extends Localizable {
	/**
	 * Returns the Locale of the guild
	 *
	 * @throws IllegalStateException If the event did not happen in a Guild
	 *
	 * @return the Locale of the guild
	 */
	@NotNull
	Locale getGuildLocale();

	/**
	 * Localizes the provided path, in the specified bundle, with the guild's locale
	 * <br>This will localize to {@code en_US} if the Guild does not have the {@code COMMUNITY} feature flag
	 *
	 * @param localizationBundle The name of the localization bundle
	 * @param localizationPath   The localization path to search for
	 * @param entries            The entries to fill the template
	 *
	 * @throws IllegalStateException If the event did not happen in a Guild
	 *
	 * @see Guild#getLocale()
	 */
	@NotNull
	default String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), localizationBundle, localizationPath, entries);
	}

	/**
	 * Localizes the provided path, in the current context's bundle, with the guild's locale
	 * <br>This will localize to {@code en_US} if the Guild does not have the {@code COMMUNITY} feature flag
	 *
	 * @param localizationPath The localization path to search for
	 * @param entries          The entries to fill the template
	 *
	 * @throws IllegalStateException If the event did not happen in a Guild
	 *
	 * @see Guild#getLocale()
	 */
	@NotNull
	default String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), getLocalizationBundle(), localizationPath, entries);
	}
}
