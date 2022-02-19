package com.freya02.botcommands.test.test;

import com.freya02.botcommands.api.Logging;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

//TODO annotation to pre-set bundle names

//Low level API
public class Localization {
	private static final Logger LOGGER = Logging.getLogger();
	private static final ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
	private static final Map<LocalizationKey, Localization> localizationMap = Collections.synchronizedMap(new HashMap<>());

	private final Map<String, LocalizationTemplate> strings = Collections.synchronizedMap(new HashMap<>());

	@SuppressWarnings("unchecked")
	private Localization(InputStream stream) throws IOException {
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			final Map<String, ?> map = new Gson().fromJson(reader, Map.class);

			discoverEntries(new LocalizationPath(), map.entrySet());
		}
	}

	@Nullable
	private static BestLocale chooseBestLocale(String baseName, Locale targetLocale) {
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, targetLocale);

		for (Locale candidateLocale : candidateLocales) {
			//Try to retrieve with the locale
			final InputStream resourceAsStream = Localization.class.getResourceAsStream("/bc_localization/" + CONTROL.toBundleName(baseName, candidateLocale) + ".json");

			if (resourceAsStream != null) {
				return new BestLocale(candidateLocale, resourceAsStream);
			}
		}

		return null;
	}

	@Nullable
	private static Localization retrieveBundle(String baseName, Locale targetLocale) throws IOException {
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, targetLocale);

		//TODO refactor in "chooseBestLocale", then determine if it has degraded
		for (int i = 0, candidateLocalesSize = Math.max(candidateLocales.size() - 1, 1); i < candidateLocalesSize; i++) {
			final Locale candidateLocale = candidateLocales.get(i);

			//Try to retrieve with the locale
			final InputStream resourceAsStream = Localization.class.getResourceAsStream("/bc_localization/" + CONTROL.toBundleName(baseName, candidateLocale) + ".json");

			if (resourceAsStream != null) {
				return new Localization(resourceAsStream);
			} else if (!targetLocale.toString().isEmpty() && !candidateLocales.get(i + 1).toString().isEmpty()) { //Not default
				LOGGER.warn("Unable to find bundle '{}' with locale '{}', falling back to {}", baseName, candidateLocale, candidateLocales.get(i + 1));
			}
		}

		if (!targetLocale.toString().isEmpty()) {
			LOGGER.warn("Unable to find bundle '{}' with locale '{}', tried: {}", baseName, targetLocale, candidateLocales.subList(0, candidateLocales.size() - 1));
		} else {
			LOGGER.warn("Unable to find bundle '{}' with root locale", baseName);
		}

		return null;
	}

	@Nullable
	public static Localization getInstance(String bundleName, Locale locale) {
		return localizationMap.computeIfAbsent(new LocalizationKey(bundleName, locale), l -> {
			try {
				return retrieveBundle(bundleName, locale);
			} catch (Exception e) {
				throw new RuntimeException("Unable to get bundle '%s' for locale '%s'".formatted(bundleName, locale), e);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void discoverEntries(LocalizationPath currentPath, Set<? extends Map.Entry<String, ?>> entries) {
		for (Map.Entry<String, ?> entry : entries) {
			if (entry.getValue() instanceof Map<?, ?> map) {
				discoverEntries(
						currentPath.resolve(entry.getKey()),
						((Map<String, ?>) map).entrySet()
				);
			} else {
				final String key = currentPath.resolve(entry.getKey()).toString();
				final LocalizationTemplate value = new LocalizationTemplate((String) entry.getValue());

				if (strings.put(key, value) != null) {
					throw new IllegalStateException("Got two same localization keys: '" + key + "'");
				}
			}
		}
	}

	@Nullable
	public LocalizationTemplate get(String query) {
		return strings.get(query);
	}

	private record LocalizationKey(String bundleName, Locale locale) {}

	private record BestLocale(Locale locale, InputStream inputStream) {}

	public record Entry(String key, String value) {
		/**
		 * Create a new localization entry, this binds a key (from a templated string) into a value
		 * <b>Highly recommended to use this method with a static import</b>
		 *
		 * @param key   The key from the templated string
		 * @param value The value to assign it to
		 * @return The entry
		 */
		public static Entry entry(String key, String value) {
			return new Entry(key, value);
		}
	}
}
