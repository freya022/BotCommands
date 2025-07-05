package io.github.freya022.botcommands.api.localization

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.arguments.factories.FormattableArgumentFactory
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import org.jetbrains.annotations.Unmodifiable
import java.util.*

/**
 * Service to retrieve [Localization] instances with the requested name and locale.
 */
@InterfacedService(acceptMultiple = false)
interface LocalizationService {
    /**
     * Gets the localization instance for the specified bundle name and locale.
     *
     * This cycles through all the available [LocalizationMap providers][LocalizationMapProvider]
     * until one returns a valid localization bundle.
     *
     * @param baseName The name of the bundle
     * @param locale   The locale of the bundle
     *
     * @return The localization instance for this bundle
     */
    fun getInstance(baseName: String, locale: Locale): Localization?

    /**
     * Invalidates all the localization bundles with the specified base name.
     *
     * @param baseName The base name of the bundles to invalidate
     */
    fun invalidateLocalization(baseName: String)

    /**
     * Invalidates the localization bundles with the specified base name and locale.
     *
     * @param baseName The base name of the bundles to invalidate
     * @param locale   The locale of the bundle to invalidate
     */
    fun invalidateLocalization(baseName: String, locale: Locale)

    /**
     * Returns an unmodifiable view of [LocalizationMap providers][LocalizationMapProvider].
     */
    fun getMappingProviders(): @Unmodifiable Collection<LocalizationMapProvider>

    /**
     * Returns an unmodifiable view of [LocalizationMap readers][LocalizationMapReader].
     */
    fun getMappingReaders(): @Unmodifiable Collection<LocalizationMapReader>

    /**
     * Returns an unmodifiable view of [FormattableArgument factories][FormattableArgumentFactory].
     */
    fun getFormattableArgumentFactories(): @Unmodifiable Collection<FormattableArgumentFactory>
}

