package com.freya02.botcommands.api.localization;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

//TODO
public interface GuildLocalizable extends Localizable {
	/**
	 * TODO
	 *
	 * @throws IllegalStateException If the event did not happen in a Guild
	 *
	 * @return
	 */
	@NotNull
	Locale getGuildLocale();

	/**
	 * This will localize to {@code en_US} if the Guild does not have the {@code COMMUNITY} feature flag
	 *
	 * TODO
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
	 * This will localize to {@code en_US} if the Guild does not have the {@code COMMUNITY} feature flag
	 *
	 * TODO
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
