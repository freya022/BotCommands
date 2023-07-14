package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Class which contains all the {@link LocalizationMapProvider}.
 * <br>This is mainly used by the localization map providers themselves, or by {@link Localization} as to cycle through all providers until one returns a valid {@link LocalizationMap}.
 */
public final class LocalizationMapProviders {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Set<LocalizationMapProvider> providers = new HashSet<>();

	@Contract(pure = true)
	@NotNull
	@UnmodifiableView
	public static Collection<LocalizationMapProvider> getProviders() {
		return Collections.unmodifiableSet(providers);
	}

	/**
	 * Cycles through all the registered providers with the specified base name and locale,
	 * and returns a {@link LocalizationMap} when a provider returns one, returns null otherwise
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale for the localization bundle, may not be the same as the one in {@link LocalizationMap#effectiveLocale()}
	 *
	 * @return a {@link LocalizationMap} if a provider returned one, {@code null} otherwise
	 */
	@Nullable
	public static LocalizationMap cycleProviders(@NotNull String baseName, @NotNull Locale locale) {
		for (LocalizationMapProvider provider : providers) {
			try {
				final LocalizationMap bundle = provider.getBundle(baseName, locale);

				if (bundle != null) {
					return bundle;
				}
			} catch (Exception e) {
				LOGGER.error("An error occurred while getting a bundle '{}' with locale '{}' with provider '{}'", baseName, locale, provider.getClass().getName());
			}
		}

		return null;
	}

	/**
	 * Cycles through all the registered providers with the specified base name and locale,
	 * and returns a {@link LocalizationMap} when a provider returns one, returns null otherwise.
	 * <br>This method uses {@link LocalizationMapProvider#getBundleNoParent(String, Locale)} instead.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale for the localization bundle, may not be the same as the one in {@link LocalizationMap#effectiveLocale()}
	 *
	 * @return a {@link LocalizationMap} if a provider returned one, {@code null} otherwise
	 */
	@Nullable
	public static LocalizationMap cycleProvidersNoParent(@NotNull String baseName, @NotNull Locale locale) throws IOException {
		for (LocalizationMapProvider provider : providers) {
			final LocalizationMap bundle = provider.getBundleNoParent(baseName, locale);

			if (bundle != null) {
				return bundle;
			}
		}

		return null;
	}

	/**
	 * Registers a new {@link LocalizationMapProvider}
	 * <br>The new provider may not be taken into account for already existing localization maps, in which case use {@link Localization#invalidateLocalization(String)}
	 *
	 * @param provider The {@link LocalizationMapProvider} to add
	 */
	public static void registerProvider(@NotNull LocalizationMapProvider provider) {
		providers.add(provider);
	}

	static {
		registerProvider(new DefaultLocalizationMapProvider());
	}
}
