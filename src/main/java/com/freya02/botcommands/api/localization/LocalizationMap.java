package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * Interface specifying getters for the effective locale and template map
 */
public interface LocalizationMap {
	/**
	 * Returns the effective locale.
	 * <br>This locale must be the one of the file which has been loaded successfully.
	 * <br><b>It does not have to be the locale passed to {@link LocalizationMapProvider#getBundle(String, Locale)}</b>.
	 *
	 * @return The effective locale for this localization map
	 */
	@NotNull Locale effectiveLocale();

	/**
	 * Returns the localization template map.
	 * <br>The key is the localization path, such as {@code my_command.name},
	 * and the value is a LocalizationTemplate of your choice, possibly {@link DefaultLocalizationTemplate}.
	 *
	 * @return The localization template map
	 */
	@NotNull Map<String, ? extends LocalizationTemplate> templateMap();
}
