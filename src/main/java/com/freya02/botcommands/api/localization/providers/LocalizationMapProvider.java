package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.api.localization.LocalizationMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Interface for localization bundle providers to implement.
 * <br>This supplies a {@link LocalizationMap} for the specified base name and locale, which may or may not inherit from parent bundles.
 *
 * <p>Localization map providers may need a mechanism so that a provider cannot read another file which is not in the correct format,
 * solutions could include having different extensions, or if you use a different format such as YAML or XML, or having your localization files in different directories.
 */
@InterfacedService(acceptMultiple = true)
public interface LocalizationMapProvider {
	ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

	/**
	 * Loads a localization map with the specified name and requested locale.
	 * <br>This may return a localization bundle with a parent locale, which must be returned by {@link LocalizationMap#effectiveLocale()}.
	 * <br>This may include parent bundles.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale
	 *
	 * @return A {@link LocalizationMap} instance with the requested data,
	 * or {@code null} if no bundle could be read, or an exception happened.
	 */
	@Nullable
	LocalizationMap getBundle(@NotNull String baseName, @NotNull Locale locale); //TODO rename to getBundleOrParent

	/**
	 * Loads a localization map with the specified name and requested locale.
	 * <br>This may return a localization bundle with a parent locale, which must be returned by {@link LocalizationMap#effectiveLocale()}.
	 * <br>This must <b>NOT</b> include parent bundles.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale
	 *
	 * @return A {@link LocalizationMap} instance with the requested data,
	 * or {@code null} if no bundle could be read, or an exception happened.
	 */
	@Nullable
	LocalizationMap getBundleNoParent(@NotNull String baseName, @NotNull Locale locale); //TODO rename to getBundle
}
