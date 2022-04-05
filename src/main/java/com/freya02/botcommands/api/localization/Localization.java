package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.localization.providers.LocalizationBundleProviders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

//TODO docs
//Low level API
public class Localization {
	private static final Logger LOGGER = Logging.getLogger();
	private static final ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
	private static final Map<String, Map<Locale, Localization>> localizationMap = Collections.synchronizedMap(new HashMap<>());

	private final Map<String, ? extends LocalizationTemplate> templateMap;
	private final Locale effectiveLocale;

	private Localization(@NotNull LocalizationBundle bundle) {
		this.effectiveLocale = bundle.getEffectiveLocale();
		this.templateMap = bundle.getTemplateMap();
	}

	@Nullable
	private static BestLocale chooseBestLocale(String baseName, Locale targetLocale) throws IOException {
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, targetLocale);

		for (Locale candidateLocale : candidateLocales) {
			//Try to retrieve with the locale
			final LocalizationBundle localizationBundle = LocalizationBundleProviders.cycleProviders(baseName, candidateLocale);

			if (localizationBundle != null) {
				if (!localizationBundle.getEffectiveLocale().equals(candidateLocale)) {
					throw new IllegalArgumentException("LocalizationBundle locale '%s' differ from requested locale '%s'".formatted(localizationBundle.getEffectiveLocale(), candidateLocale));
				}

				return new BestLocale(localizationBundle.getEffectiveLocale(), localizationBundle);
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

			return new Localization(bestLocale.bundle());
		}
	}

	public static void invalidateLocalization(@NotNull String baseName) {
		localizationMap.remove(baseName);
	}

	public static void invalidateLocalization(@NotNull String baseName, @NotNull Locale locale) {
		localizationMap.computeIfAbsent(baseName, x -> Collections.synchronizedMap(new HashMap<>())).remove(locale);
	}

	@Nullable
	public static Localization getInstance(@NotNull String baseName, @NotNull Locale locale) {
		final Map<Locale, Localization> localeMap = localizationMap.computeIfAbsent(baseName, x -> Collections.synchronizedMap(new HashMap<>()));
		final Localization value = localeMap.get(locale);

		if (value != null) {
			return value;
		} else {
			try {
				final Localization newValue = retrieveBundle(baseName, locale);
				localeMap.put(locale, newValue);

				return newValue;
			} catch (Exception e) {
				throw new RuntimeException("Unable to get bundle '%s' for locale '%s'".formatted(baseName, locale), e);
			}
		}
	}

	@NotNull
	@UnmodifiableView
	public Map<String, ? extends LocalizationTemplate> getTemplateMap() {
		return Collections.unmodifiableMap(templateMap);
	}

	@Nullable
	public LocalizationTemplate get(String path) {
		return templateMap.get(path);
	}

	public Locale getEffectiveLocale() {
		return effectiveLocale;
	}

	private record BestLocale(Locale locale, LocalizationBundle bundle) {}

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
