package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.localization.LocalizationMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Interface for localization bundle providers to implement.
 * <br>This supplies a {@link LocalizationMap} for the specified base name and locale, which may or may not inherit from parent bundles.
 *
 * <p>Localization map providers may need a mechanism so that a provider cannot read another file which is not in the correct format,
 * solutions could include having different extensions, if you use a different format such as YAML or XML, or having your localization files in different directories.
 */
public interface LocalizationMapProvider {
	ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

	/**
	 * Utility method to append a path component to an existing path, this is simply {@code path + '.' + other}.
	 *
	 * @param path  The current path
	 * @param other The other path component
	 *
	 * @return The new path
	 */
	default String appendPath(String path, String other) {
		if (path.isBlank()) return other;

		return path + '.' + other;
	}

	/**
	 * Returns the bundle name with the specified base name and Locale.
	 * <br>This follows the same naming as the one used by {@link ResourceBundle},
	 * such as {@code baseName_en_US}, or {@code baseName_fr} depending on the locale supplied
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale
	 *
	 * @return The formatted name of the bundle
	 */
	@NotNull
	default String getBundleName(@NotNull String baseName, @NotNull Locale locale) {
		return CONTROL.toBundleName(baseName, locale);
	}

	/**
	 * Loads a localization map with the specified name and requested locale.
	 * <br>This may return a localization bundle with a parent locale, which must be returned by {@link LocalizationMap#effectiveLocale()}.
	 * <br>This may include parent bundles.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale
	 *
	 * @return A {@link LocalizationMap} instance with the requested data
	 *
	 * @throws IOException In case loading fails
	 */
	@Nullable
	LocalizationMap getBundle(@NotNull String baseName, @NotNull Locale locale) throws IOException;

	/**
	 * Loads a localization map with the specified name and requested locale.
	 * <br>This may return a localization bundle with a parent locale, which must be returned by {@link LocalizationMap#effectiveLocale()}.
	 * <br>This must <b>NOT</b> include parent bundles.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale
	 *
	 * @return A {@link LocalizationMap} instance with the requested data
	 *
	 * @throws IOException In case loading fails
	 */
	@Nullable
	LocalizationMap getBundleNoParent(@NotNull String baseName, @NotNull Locale locale) throws IOException;
}
