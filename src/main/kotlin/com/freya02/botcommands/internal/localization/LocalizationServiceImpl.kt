package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.api.core.utils.logger
import com.freya02.botcommands.api.localization.LocalizationMap
import com.freya02.botcommands.api.localization.LocalizationService
import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider
import com.freya02.botcommands.api.localization.providers.LocalizationMapProviders
import com.freya02.botcommands.api.localization.readers.LocalizationMapReader
import com.freya02.botcommands.api.localization.readers.LocalizationMapReaders
import com.freya02.botcommands.internal.commands.application.localization.BCLocalizationFunction
import com.freya02.botcommands.internal.core.SingleLogger
import mu.KotlinLogging
import org.jetbrains.annotations.UnmodifiableView
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger<LocalizationService>()

@BService
@ServiceType(LocalizationService::class)
internal class LocalizationServiceImpl internal constructor(
    private val localizationMapProviders: LocalizationMapProviders,
    private val localizationMapReader: LocalizationMapReaders
) : LocalizationService {
    private class BestLocale(val locale: Locale, val bundle: LocalizationMap)

    private val control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT)

    private val lock = ReentrantLock()
    private val localizationMap: MutableMap<String, MutableMap<Locale, LocalizationImpl>> = hashMapOf()

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
    override fun getInstance(baseName: String, locale: Locale): LocalizationImpl? {
        try {
            lock.withLock {
                val localeMap = localizationMap.computeIfAbsent(baseName) { hashMapOf() }

                return localeMap.getOrPut(locale) {
                    retrieveBundle(baseName, locale) ?: return null
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Unable to get bundle '$baseName' for locale '$locale'", e)
        }
    }

    private fun retrieveBundle(baseName: String, targetLocale: Locale): LocalizationImpl? {
        return when (val bestLocale = chooseBestLocale(baseName, targetLocale)) {
            null -> {
                if (SingleLogger.current().tryLog(baseName)) logger.warn("Could not find localization resources for '{}'", baseName)

                null
            }
            else -> {
                if (bestLocale.locale != targetLocale) { //Not default
                    if (bestLocale.locale.toString().isEmpty()) { //neutral lang
                        if (SingleLogger.current().tryLog(baseName, targetLocale.toLanguageTag()))
                            logger.warn("Unable to find bundle '{}' with locale '{}', falling back to neutral lang", baseName, targetLocale)
                    } else {
                        if (SingleLogger.current().tryLog(baseName, targetLocale.toLanguageTag(), bestLocale.locale.toLanguageTag()))
                            logger.warn("Unable to find bundle '{}' with locale '{}', falling back to '{}'", baseName, targetLocale, bestLocale.locale)
                    }
                }

                LocalizationImpl(bestLocale.bundle)
            }
        }
    }

    private fun chooseBestLocale(baseName: String, targetLocale: Locale): BestLocale? {
        val candidateLocales = control.getCandidateLocales(baseName, targetLocale)
        for (candidateLocale in candidateLocales) {
            //Try to retrieve with the locale
            val localizationBundle = localizationMapProviders.cycleProvidersWithParents(baseName, candidateLocale)
            if (localizationBundle != null) {
                return BestLocale(localizationBundle.effectiveLocale(), localizationBundle)
            }
        }

        return null
    }

    /**
     * Invalidates all the localization bundles with the specified base name
     *
     * @param baseName The base name of the bundles to invalidate
     */
    override fun invalidateLocalization(baseName: String) {
        SingleLogger[BCLocalizationFunction::class].clear()
        SingleLogger.current().clear()
        localizationMap.remove(baseName)
    }

    /**
     * Invalidates the localization bundles with the specified base name and locale
     *
     * @param baseName The base name of the bundles to invalidate
     * @param locale   The locale of the bundle to invalidate
     */
    override fun invalidateLocalization(baseName: String, locale: Locale) {
        SingleLogger[BCLocalizationFunction::class].clear()
        SingleLogger.current().clear()

        localizationMap[baseName]?.remove(locale)
    }

    override fun getMappingProviders(): @UnmodifiableView Collection<LocalizationMapProvider> = localizationMapProviders.providers

    override fun getMappingReaders(): @UnmodifiableView Collection<LocalizationMapReader> = localizationMapReader.readers
}