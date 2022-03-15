package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.api.Logging;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

//TODO docs
//Low level API
public class Localization {
	private static final Logger LOGGER = Logging.getLogger();
	private static final ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
	private static final Map<LocalizationKey, Localization> localizationMap = new HashMap<>();

	private final Map<String, LocalizationTemplate> strings = Collections.synchronizedMap(new HashMap<>());
	private final Locale effectiveLocale;
	private final boolean isInheritable;

	@SuppressWarnings("unchecked")
	private Localization(String baseName, Locale effectiveLocale, InputStream stream) throws IOException {
		this.effectiveLocale = effectiveLocale;

		final Map<String, LocalizationTemplate> tempMap = new HashMap<>(); //Need this so it doesn't throw on duplicated strings
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			final Map<String, ?> map = new Gson().fromJson(reader, Map.class);

			this.isInheritable = map.get("inheritable") != null //Default to false
					&& ((Boolean) map.get("inheritable"));

			discoverEntries(tempMap, effectiveLocale, new LocalizationPath(), map.entrySet());
		}

		//Add parent localization first
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, effectiveLocale);
		Collections.reverse(candidateLocales); //Need to add the most precise locals last

		for (Locale candidateLocale : candidateLocales) {
			if (candidateLocale.equals(effectiveLocale)) continue;

			final Localization instance = Localization.getInstance(baseName, candidateLocale);
			if (instance == null) continue; //Parent localization failed to load

			this.strings.putAll(instance.strings);

			if (instance.isInheritable) { //Prevent purposefully inherited strings to be shown as warnings
				tempMap.putAll(instance.strings);
			}
		}

		//Check if inherited strings are missing from here
		final ArrayList<String> inheritedKeys = new ArrayList<>(strings.keySet());
		inheritedKeys.removeAll(tempMap.keySet()); //Remove keys that got read

		if (!inheritedKeys.isEmpty()) {
			LOGGER.warn("Bundle '{}' with locale '{}' is missing strings:\n{}", baseName, effectiveLocale, String.join("\n", inheritedKeys));
		}

		//Put this bundle's strings
		this.strings.putAll(tempMap);
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
		final BestLocale bestLocale = chooseBestLocale(baseName, targetLocale);

		if (bestLocale == null) {
			LOGGER.warn("Could not find localization resources for '{}'", baseName);

			return null;
		} else {
			if (!bestLocale.locale().equals(targetLocale)) { //Not default
				if (bestLocale.locale().toString().isEmpty()) { //neutral lang
					LOGGER.warn("Unable to find bundle '{}' with locale '{}', falling back to neutral lang", baseName, targetLocale);
				} else {
					LOGGER.warn("Unable to find bundle '{}' with locale '{}', falling back to '{}'", baseName, targetLocale, bestLocale.locale());
				}
			}

			return new Localization(baseName, bestLocale.locale(), bestLocale.inputStream());
		}
	}

	@Nullable
	public static synchronized Localization getInstance(@NotNull String bundleName, @NotNull Locale locale) {
		final LocalizationKey key = new LocalizationKey(bundleName, locale);
		final Localization value = localizationMap.get(key);

		if (value != null) {
			return value;
		} else {
			try {
				final Localization newValue = retrieveBundle(bundleName, locale);
				localizationMap.put(key, newValue);

				return newValue;
			} catch (Exception e) {
				throw new RuntimeException("Unable to get bundle '%s' for locale '%s'".formatted(bundleName, locale), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void discoverEntries(Map<String, LocalizationTemplate> strings, Locale effectiveLocale, LocalizationPath currentPath, Set<? extends Map.Entry<String, ?>> entries) {
		for (Map.Entry<String, ?> entry : entries) {
			if (entry.getValue() instanceof Map<?, ?> map) {
				discoverEntries(
						strings,
						effectiveLocale,
						currentPath.resolve(entry.getKey()),
						((Map<String, ?>) map).entrySet()
				);
			} else {
				final String key = currentPath.resolve(entry.getKey()).toString();
				if (key.equals("inheritable")) continue; //Skip inheritable property

				final LocalizationTemplate value = new LocalizationTemplate((String) entry.getValue(), effectiveLocale);

				if (strings.put(key, value) != null) {
					throw new IllegalStateException("Got two same localization keys: '" + key + "'");
				}
			}
		}
	}

	@Nullable
	public LocalizationTemplate get(String path) {
		return strings.get(path);
	}

	public Locale getEffectiveLocale() {
		return effectiveLocale;
	}

	private record LocalizationKey(String bundleName, Locale locale) {}

	private record BestLocale(Locale locale, InputStream inputStream) {}

	public record Entry(String key, Object value) {
		/**
		 * Create a new localization entry, this binds a key (from a templated string) into a value
		 * <b>Highly recommended to use this method with a static import</b>
		 *
		 * @param key   The key from the templated string
		 * @param value The value to assign it to
		 * @return The entry
		 */
		@NotNull
		public static Entry entry(@NotNull String key, @NotNull Object value) {
			return new Entry(key, value);
		}
	}
}
