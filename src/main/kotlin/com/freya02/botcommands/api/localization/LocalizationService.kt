package com.freya02.botcommands.api.localization

import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider
import com.freya02.botcommands.api.localization.readers.LocalizationMapReader
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

/**
 * Service to retrieve [Localization] instances with the requested name and locale.
 */
@InterfacedService(acceptMultiple = false)
interface LocalizationService {
    /**
     * Gets the localization instance for the specified bundle name and locale.
     *
     * This cycles through all the available [LocalizationMapProviders][LocalizationMapProvider] until one returns a valid localization bundle.
     *
     * @param baseName The name of the bundle
     * @param locale   The locale of the bundle
     *
     * @return The localization instance for this bundle
     */
    fun getInstance(baseName: String, locale: Locale): Localization?

    /**
     * Invalidates all the localization bundles with the specified base name
     *
     * @param baseName The base name of the bundles to invalidate
     */
    fun invalidateLocalization(baseName: String)

    /**
     * Invalidates the localization bundles with the specified base name and locale
     *
     * @param baseName The base name of the bundles to invalidate
     * @param locale   The locale of the bundle to invalidate
     */
    fun invalidateLocalization(baseName: String, locale: Locale)

    /**
     * Returns an unmodifiable view of [localization map providers][LocalizationMapProvider]
     */
    fun getMappingProviders(): @UnmodifiableView Collection<LocalizationMapProvider>

    /**
     * Returns an unmodifiable view of [localization map readers][LocalizationMapReader]
     */
    fun getMappingReaders(): @UnmodifiableView Collection<LocalizationMapReader>
}

