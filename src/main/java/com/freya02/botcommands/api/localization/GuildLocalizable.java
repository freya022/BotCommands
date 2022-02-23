package com.freya02.botcommands.api.localization;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface GuildLocalizable extends Localizable {
	@NotNull
	Locale getGuildLocale();

	/**
	 * This will localize to {@code en_US} if the Guild does not have the {@code COMMUNITY} feature flag
	 *
	 * TODO
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
	 * @see Guild#getLocale()
	 */
	@NotNull
	default String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), getLocalizationBundle(), localizationPath, entries);
	}
}
