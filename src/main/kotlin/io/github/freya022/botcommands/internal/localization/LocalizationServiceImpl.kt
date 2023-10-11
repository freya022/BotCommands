package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.logger
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.arguments.factories.FormattableArgumentFactory
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProviders
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReaders
import io.github.freya022.botcommands.internal.commands.application.localization.BCLocalizationFunction
import io.github.freya022.botcommands.internal.core.SingleLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.annotations.UnmodifiableView
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger<LocalizationService>()

@BService
internal class LocalizationServiceImpl internal constructor(
    context: BContext,
    private val localizationMapProviders: LocalizationMapProviders,
    private val localizationMapReader: LocalizationMapReaders
) : LocalizationService {
    private val formattableArgumentFactories = Collections.unmodifiableList(context.getInterfacedServices<FormattableArgumentFactory>())

    private val lock = ReentrantLock()
    private val localizationMap: MutableMap<String, MutableMap<Locale, Localization>> = ConcurrentHashMap()

    override fun getInstance(baseName: String, locale: Locale): Localization? {
        try {
            val localeMap = localizationMap.computeIfAbsent(baseName) { ConcurrentHashMap() }

            // Fast path
            localeMap[locale]?.let { return it }

            // Slow path
            return lock.withLock {
                localeMap.getOrPut(locale) {
                    retrieveLocalization(baseName, locale) ?: return null
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Unable to get bundle '$baseName' for locale '$locale'", e)
        }
    }

    private fun retrieveLocalization(baseName: String, targetLocale: Locale): LocalizationImpl? {
        return when (val localizationMap = localizationMapProviders.cycleProvidersWithParents(baseName, targetLocale)) {
            null -> {
                if (SingleLogger.current().tryLog(baseName)) logger.warn("Could not find localization resources for '{}'", baseName)

                null
            }
            else -> {
                if (localizationMap.effectiveLocale != targetLocale) { //Not default
                    if (localizationMap.effectiveLocale.toString().isEmpty()) { //neutral lang
                        if (SingleLogger.current().tryLog(baseName, targetLocale.toLanguageTag()))
                            logger.warn("Unable to find bundle '{}' with locale '{}', falling back to neutral lang", baseName, targetLocale)
                    } else {
                        if (SingleLogger.current().tryLog(baseName, targetLocale.toLanguageTag(), localizationMap.effectiveLocale.toLanguageTag()))
                            logger.warn("Unable to find bundle '{}' with locale '{}', falling back to '{}'", baseName, targetLocale, localizationMap.effectiveLocale)
                    }
                }

                LocalizationImpl(localizationMap)
            }
        }
    }

    override fun invalidateLocalization(baseName: String) {
        SingleLogger[BCLocalizationFunction::class].clear()
        SingleLogger.current().clear()
        localizationMap.remove(baseName)
    }

    override fun invalidateLocalization(baseName: String, locale: Locale) {
        SingleLogger[BCLocalizationFunction::class].clear()
        SingleLogger.current().clear()

        localizationMap[baseName]?.remove(locale)
    }

    override fun getMappingProviders(): @UnmodifiableView Collection<LocalizationMapProvider> = localizationMapProviders.providers

    override fun getMappingReaders(): @UnmodifiableView Collection<LocalizationMapReader> = localizationMapReader.readers

    override fun getFormattableArgumentFactories(): Collection<FormattableArgumentFactory> = formattableArgumentFactories
}