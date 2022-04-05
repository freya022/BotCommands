package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Enables the subclass to localize strings with the current guild locale
 */
public interface UserLocalizable extends Localizable {
	/**
	 * Returns the Locale of the user
	 *
	 * @return The Locale of the user
	 */
	@NotNull
	Locale getUserLocale();

	/**
	 * Localizes the provided path, in the specified bundle, with the user's locale
	 *
	 * @param localizationBundle The name of the localization bundle
	 * @param localizationPath   The localization path to search for
	 * @param entries            The entries to fill the template
	 *
	 * @return The localized string
	 */
	@NotNull
	default String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getUserLocale(), localizationBundle, localizationPath, entries);
	}

	/**
	 * Localizes the provided path, in the current context's bundle, with the user's locale
	 *
	 * @param localizationPath The localization path to search for
	 * @param entries          The entries to fill the template
	 *
	 * @return The localized string
	 */
	@NotNull
	default String localizeUser(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getUserLocale(), getLocalizationBundle(), localizationPath, entries);
	}
}
