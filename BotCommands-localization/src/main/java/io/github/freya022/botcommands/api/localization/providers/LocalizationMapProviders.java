package io.github.freya022.botcommands.api.localization.providers;

import io.github.freya022.botcommands.api.core.Logging;
import io.github.freya022.botcommands.api.core.service.ServiceContainer;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.localization.LocalizationMap;
import io.github.freya022.botcommands.api.localization.LocalizationService;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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
@NullMarked
public final class LocalizationMapProviders {
    private static final Logger LOGGER = Logging.getLogger();

    private final ServiceContainer serviceContainer;

    @Nullable
    private Collection<LocalizationMapProvider> providers = null;

    public LocalizationMapProviders(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
    }

    @Unmodifiable
    public Collection<LocalizationMapProvider> getProviders() {
        if (providers == null)
            providers = Collections.unmodifiableList(serviceContainer.getInterfacedServices(LocalizationMapProvider.class));
        return providers;
    }

    /**
     * Cycles through all the registered providers with the specified base name and locale,
     * and returns a {@link LocalizationMap} when a provider returns one,
     * returns null otherwise.
     *
     * <p>This method should also try to get bundles with parent locales.
     *
     * @param baseName The base name of the localization bundle
     * @param locale   The requested locale for the localization bundle,
     *                 which may not be the same as the one in {@link LocalizationMap#getEffectiveLocale()}
     *
     * @return a {@link LocalizationMap} if a provider returned one, {@code null} otherwise
     */
    @Nullable
    public LocalizationMap cycleProvidersWithParents(String baseName, Locale locale) {
        for (LocalizationMapProvider provider : getProviders()) {
            try {
                final LocalizationMap bundle = provider.fromBundleOrParent(baseName, locale);

                if (bundle != null) {
                    return bundle;
                }
            } catch (Exception e) {
                LOGGER.warn("An error occurred while getting a bundle w/ parents '{}' with locale '{}' with provider '{}'", baseName, locale, provider.getClass().getName(), e);
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
    public LocalizationMap cycleProviders(String baseName, Locale locale) {
        for (LocalizationMapProvider provider : getProviders()) {
            try {
                final LocalizationMap bundle = provider.fromBundle(baseName, locale);

                if (bundle != null) {
                    return bundle;
                }
            } catch (Exception e) {
                LOGGER.warn("An error occurred while getting a bundle '{}' with locale '{}' with provider '{}'", baseName, locale, provider.getClass().getName(), e);
            }
        }

        return null;
    }
}
