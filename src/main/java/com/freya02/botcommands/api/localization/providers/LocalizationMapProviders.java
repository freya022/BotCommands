package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.core.service.ServiceContainer;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.localization.LocalizationMap;
import com.freya02.botcommands.api.localization.LocalizationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Class which contains all the {@link LocalizationMapProvider}.
 * <br>This is mainly used by the localization map providers themselves,
 * or by {@link LocalizationService} as to cycle through all providers
 * until one returns a valid {@link LocalizationMap}.
 */
@BService
public final class LocalizationMapProviders {
	private static final Logger LOGGER = Logging.getLogger();

	private final ServiceContainer serviceContainer;

	private Collection<LocalizationMapProvider> providers = null;

	public LocalizationMapProviders(ServiceContainer serviceContainer) {
		this.serviceContainer = serviceContainer;
	}

	@NotNull
	@UnmodifiableView
	public Collection<LocalizationMapProvider> getProviders() {
		if (providers == null)
			providers = serviceContainer.getInterfacedServices(LocalizationMapProvider.class);
		return Collections.unmodifiableCollection(providers);
	}

	/**
	 * Cycles through all the registered providers with the specified base name and locale,
	 * and returns a {@link LocalizationMap} when a provider returns one,
	 * returns null otherwise.
	 *
	 * <p>This method also tries to get bundles with parent locales.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale for the localization bundle,
	 *                 which may not be the same as the one in {@link LocalizationMap#getEffectiveLocale()}
	 *
	 * @return a {@link LocalizationMap} if a provider returned one, {@code null} otherwise
	 */
	@Nullable
	public LocalizationMap cycleProvidersWithParents(@NotNull String baseName, @NotNull Locale locale) {
		for (LocalizationMapProvider provider : getProviders()) {
			try {
				final LocalizationMap bundle = provider.fromBundleOrParent(baseName, locale);

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
	 * and returns a {@link LocalizationMap} when a provider returns one,
	 * returns null otherwise.
	 *
	 * <p>This method will only use the passed locale.
	 *
	 * @param baseName The base name of the localization bundle
	 * @param locale   The requested locale for the localization bundle,
	 *                 which may not be the same as the one in {@link LocalizationMap#getEffectiveLocale()}
	 *
	 * @return a {@link LocalizationMap} if a provider returned one, {@code null} otherwise
	 */
	@Nullable
	public LocalizationMap cycleProviders(@NotNull String baseName, @NotNull Locale locale) {
		for (LocalizationMapProvider provider : getProviders()) {
			try {
				final LocalizationMap bundle = provider.fromBundle(baseName, locale);

				if (bundle != null) {
					return bundle;
				}
			} catch (Exception e) {
				LOGGER.error("An error occurred while getting a bundle '{}' with locale '{}' with provider '{}'", baseName, locale, provider.getClass().getName());
			}
		}

		return null;
	}
}
