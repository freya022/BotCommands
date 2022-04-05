package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Enables the subclass to localize strings with any locale
 */
public interface Localizable {
	/**
	 * Returns the localization bundle of the current context
	 *
	 * @return The localization bundle for this context
	 */
	@NotNull
	String getLocalizationBundle();

	/**
	 * Localizes the provided path, in the specified bundle, with the provided locale
	 *
	 * @param locale             The Locale to use when fetching the localization bundle
	 * @param localizationBundle The name of the localization bundle
	 * @param localizationPath   The localization path to search for
	 * @param entries            The entries to fill the template
	 *
	 * @return The localized string
	 */
	@NotNull
	String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * Localizes the provided path, in the current context's bundle, with the provided locale
	 *
	 * @param locale             The Locale to use when fetching the localization bundle
	 * @param localizationPath   The localization path to search for
	 * @param entries            The entries to fill the template
	 *
	 * @return The localized string
	 */
	@NotNull
	String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);
}
