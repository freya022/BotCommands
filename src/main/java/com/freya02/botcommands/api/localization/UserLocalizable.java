package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

//TODO
public interface UserLocalizable extends Localizable {
	@NotNull
	Locale getUserLocale();

	@NotNull
	default String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getUserLocale(), localizationBundle, localizationPath, entries);
	}

	@NotNull
	default String localizeUser(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getUserLocale(), getLocalizationBundle(), localizationPath, entries);
	}
}
