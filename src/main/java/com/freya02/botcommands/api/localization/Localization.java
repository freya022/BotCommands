package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.localization.providers.DefaultLocalizationMapProvider;
import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider;
import com.freya02.botcommands.api.localization.providers.LocalizationMapProviders;
import com.freya02.botcommands.internal.commands.application.localization.BCLocalizationFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.*;

/**
 * Provides a low level API for localization
 * <br>You can get an instance using {@link #getInstance(String, Locale)}, as well as invalidate cached localization data, as to reload them on next use
 * <br>You can customize localization providers, as well as the localization templates they give, each provider is tested until one returns a valid localization bundle, see {@link DefaultLocalizationMapProvider} for the default specification
 * <p>You can add more localization bundle providers using {@link LocalizationMapProviders#registerProvider(LocalizationMapProvider)}
 */
public class Localization {
	private static final Logger LOGGER = Logging.getLogger();
	private static final ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
	private static final Map<String, Map<Locale, Localization>> localizationMap = Collections.synchronizedMap(new HashMap<>());

	private final Map<String, ? extends LocalizationTemplate> templateMap;
	private final Locale effectiveLocale;

	private Localization(@NotNull LocalizationMap bundle) {
		this.effectiveLocale = bundle.effectiveLocale();
		this.templateMap = bundle.templateMap();
	}

	@Nullable
	private static BestLocale chooseBestLocale(String baseName, Locale targetLocale) {
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, targetLocale);

		for (Locale candidateLocale : candidateLocales) {
			//Try to retrieve with the locale
			final LocalizationMap localizationBundle = LocalizationMapProviders.cycleProviders(baseName, candidateLocale);

			if (localizationBundle != null) {
				return new BestLocale(localizationBundle.effectiveLocale(), localizationBundle);
			}
		}

		return null;
	}

	@Nullable
	private static Localization retrieveBundle(String baseName, Locale targetLocale) {
		final BestLocale bestLocale = chooseBestLocale(baseName, targetLocale);

		if (bestLocale == null) {
			if (Logging.tryLog(baseName)) LOGGER.warn("Could not find localization resources for '{}'", baseName);

			return null;
		} else {
			if (!bestLocale.locale().equals(targetLocale)) { //Not default
				if (bestLocale.locale().toString().isEmpty()) { //neutral lang
					if (Logging.tryLog(baseName, targetLocale.toLanguageTag()))
						LOGGER.warn("Unable to find bundle '{}' with locale '{}', falling back to neutral lang", baseName, targetLocale);
				} else {
					if (Logging.tryLog(baseName, targetLocale.toLanguageTag(), bestLocale.locale.toLanguageTag()))
						LOGGER.warn("Unable to find bundle '{}' with locale '{}', falling back to '{}'", baseName, targetLocale, bestLocale.locale());
				}
			}

			return new Localization(bestLocale.bundle());
		}
	}

	/**
	 * Invalidates all the localization bundles with the specified base name
	 *
	 * @param baseName The base name of the bundles to invalidate
	 */
	public static void invalidateLocalization(@NotNull String baseName) {
		Logging.removeLogs(BCLocalizationFunction.class);
		Logging.removeLogs();
		localizationMap.remove(baseName);
	}

	/**
	 * Invalidates the localization bundles with the specified base name and locale
	 *
	 * @param baseName The base name of the bundles to invalidate
	 * @param locale   The locale of the bundle to invalidate
	 */
	public static void invalidateLocalization(@NotNull String baseName, @NotNull Locale locale) {
		Logging.removeLogs(BCLocalizationFunction.class);
		Logging.removeLogs();
		localizationMap.computeIfAbsent(baseName, x -> Collections.synchronizedMap(new HashMap<>())).remove(locale);
	}

	/**
	 * Gets the localization instance for the specified bundle name and locale
	 * <br>This cycles through all the available {@link LocalizationMapProvider LocalizationBundleProviders} until one returns a valid localization bundle
	 *
	 * @param baseName The name of the bundle
	 * @param locale   The locale of the bundle
	 *
	 * @return The localization instance for this bundle
	 */
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

	/**
	 * Returns an unmodifiable view of the <code>localization path -> LocalizationTemplate</code> map
	 *
	 * @return An unmodifiable view of the <code>localization path -> LocalizationTemplate</code> map
	 */
	@NotNull
	@UnmodifiableView
	public Map<String, ? extends LocalizationTemplate> getTemplateMap() {
		return Collections.unmodifiableMap(templateMap);
	}

	/**
	 * Returns the {@link LocalizationTemplate} for the specified localization path
	 *
	 * @param path The localization path of the template
	 *
	 * @return The {@link LocalizationTemplate} for the specified localization path
	 */
	@Nullable
	public LocalizationTemplate get(String path) {
		return templateMap.get(path);
	}

	/**
	 * Returns the effective Locale for this Localization instance
	 * <br>This might not be the same as the one supplied in {@link #getInstance(String, Locale)} due to missing bundles
	 *
	 * @return The effective Locale for this Localization instance
	 */
	public Locale getEffectiveLocale() {
		return effectiveLocale;
	}

	private record BestLocale(Locale locale, LocalizationMap bundle) {}

	public record Entry(String key, Object value) {
		/**
		 * Create a new localization entry, this binds a key (from a templated string) into a value
		 * <b>Highly recommended to use this method with a static import</b>
		 *
		 * @param key   The key from the templated string
		 * @param value The value to assign it to
		 *
		 * @return The entry
		 */
		@NotNull
		public static Entry entry(@NotNull String key, @NotNull Object value) {
			return new Entry(key, value);
		}
	}
}
