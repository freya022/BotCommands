package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface Localizable {
	String getLocalizationBundle();

	@NotNull String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	@NotNull String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);
}
