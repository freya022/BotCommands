package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public interface LocalizationBundle {
	@NotNull Locale getEffectiveLocale();

	@NotNull Map<String, ? extends LocalizationTemplate> getTemplateMap();
}
