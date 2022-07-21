package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * POJO for effective locale and localization template map
 */
public record DefaultLocalizationMap(Locale effectiveLocale,
                                     Map<String, ? extends LocalizationTemplate> templateMap) implements LocalizationMap {
	@Override
	@NotNull
	public Locale effectiveLocale() {
		return effectiveLocale;
	}

	@Override
	@NotNull
	public Map<String, ? extends LocalizationTemplate> templateMap() {
		return templateMap;
	}
}
