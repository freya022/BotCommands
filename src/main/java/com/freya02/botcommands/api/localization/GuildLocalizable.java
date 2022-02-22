package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface GuildLocalizable extends Localizable {
	@NotNull
	Locale getGuildLocale();

	@NotNull
	default String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), localizationBundle, localizationPath, entries);
	}

	@NotNull
	default String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), getLocalizationBundle(), localizationPath, entries);
	}
}
