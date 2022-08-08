package com.freya02.botcommands.internal.application.localization;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.builder.DebugBuilder;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BCLocalizationFunction implements LocalizationFunction {
	private static final Logger LOGGER = Logging.getLogger();
	private final Map<String, List<Locale>> baseNameToLocalesMap;

	public BCLocalizationFunction(BContextImpl context) {
		baseNameToLocalesMap = context.getApplicationCommandsContext().getBaseNameToLocalesMap();
	}

	@NotNull
	@Override
	public Map<DiscordLocale, String> apply(@NotNull String localizationKey) {
		final Map<DiscordLocale, String> map = new HashMap<>();

		baseNameToLocalesMap.forEach((baseName, locales) -> {
			for (Locale locale : locales) {
				final Localization instance = Localization.getInstance(baseName, locale);
				if (instance != null) {
					if (instance.getEffectiveLocale() != locale) {
						if (Logging.tryLog(baseName, locale.toLanguageTag(), instance.getEffectiveLocale().toLanguageTag())) {
							LOGGER.warn("Localization bundle '{}' with locale '{}' was specified to be valid but was not found, falling back to '{}'", baseName, locale, instance.getEffectiveLocale());
						}
					}

					final LocalizationTemplate template = instance.get(localizationKey);
					if (template != null) {
						map.put(DiscordLocale.from(locale), template.localize());
					} else if (DebugBuilder.isLogMissingLocalizationEnabled()) {
						if (Logging.tryLog(baseName, locale.toLanguageTag(), localizationKey)) {
							LOGGER.warn("Localization template '{}' could not be found in bundle '{}' with locale '{}' or below", localizationKey, baseName, locale);
						}
					}
				} else if (Logging.tryLog(baseName, locale.toLanguageTag())) {
					LOGGER.warn("Localization bundle '{}' with locale '{}' was specified to be valid but was not found.", baseName, locale);
				}
			}
		});

		return map;
	}
}
