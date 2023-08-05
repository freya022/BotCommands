package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.api.core.service.annotations.ServiceType;
import com.freya02.botcommands.api.localization.LocalizationMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Supplies {@link LocalizationMap}s for the requested bundle name and locale.
 *
 * <p>
 * <b>Usage:</b> Register your instance as a service with {@link BService},
 * and a {@link ServiceType} of {@link LocalizationMapProvider}.
 *
 * @see DefaultLocalizationMapProvider
 */
@InterfacedService(acceptMultiple = true)
public interface LocalizationMapProvider {
	ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

	/**
	 * Loads a localization map with the requested name and requested locale.
	 * <br>This may return a localization bundle with a broader locale,
	 * which will be returned by {@link LocalizationMap#effectiveLocale()}.
	 * <br>This may include parent bundles.
	 *
	 * @param baseName        The base name of the localization bundle
	 * @param requestedLocale The requested locale
	 *
	 * @return A {@link LocalizationMap} instance with the requested data,
	 * or {@code null} if no bundle could be read, or a (logged) exception happened.
	 */
	@Nullable
	LocalizationMap fromBundleOrParent(@NotNull String baseName, @NotNull Locale requestedLocale);

	/**
	 * Loads a localization map with the requested name and requested locale.
	 * <br>This may return a localization bundle with a broader locale,
	 * which must be returned by {@link LocalizationMap#effectiveLocale()}.
	 *
	 * <p><b>Note:</b> this does <b>NOT</b> include parent bundles.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale
	 *
	 * @return A {@link LocalizationMap} instance with the requested data,
	 * or {@code null} if no bundle could be read, or a (logged) exception happened.
	 */
	@Nullable
	LocalizationMap fromBundle(@NotNull String baseName, @NotNull Locale locale);
}
