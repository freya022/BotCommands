package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public class DefaultLocalizationMap implements LocalizationMap {
	private final Locale effectiveLocale;
	private final Map<String, ? extends LocalizationTemplate> templateMap;

	public DefaultLocalizationMap(Locale effectiveLocale, Map<String, ? extends LocalizationTemplate> templateMap) {
		this.effectiveLocale = effectiveLocale;
		this.templateMap = templateMap;
	}

	@Override
	@NotNull
	public Locale getEffectiveLocale() {
		return effectiveLocale;
	}

	@Override
	@NotNull
	public Map<String, ? extends LocalizationTemplate> getTemplateMap() {
		return templateMap;
	}
}
