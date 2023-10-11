package io.github.freya022.botcommands.internal.commands.application.localization

import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.SingleLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*

private val logger = KotlinLogging.logger { }

internal class BCLocalizationFunction(private val context: BContextImpl) : LocalizationFunction {
    private val localizationService: LocalizationService = context.getService<LocalizationService>()
    private val baseNameToLocalesMap: Map<String, List<Locale>> = context.applicationConfig.baseNameToLocalesMap

    override fun apply(localizationKey: String): Map<DiscordLocale, String> {
        val map: MutableMap<DiscordLocale, String> = EnumMap(DiscordLocale::class.java)

        baseNameToLocalesMap.forEach { (baseName, locales) ->
            for (locale in locales) {
                val instance = localizationService.getInstance(baseName, locale)
                if (instance != null) {
                    if (instance.effectiveLocale !== locale) {
                        SingleLogger.current().tryLog(baseName, locale.toLanguageTag(), instance.effectiveLocale.toLanguageTag()) {
                            logger.warn {
                                "Localization bundle '${baseName}' with locale '${locale}' was specified to be valid but was not found, falling back to '${instance.effectiveLocale}'"
                            }
                        }
                    }

                    val template = instance[localizationKey]
                    if (template != null) {
                        map[locale.toDiscordLocale()] = template.localize()
                    } else if (context.debugConfig.enabledMissingLocalizationLogs) {
                        SingleLogger.current().tryLog(baseName, locale.toLanguageTag(), localizationKey) {
                            logger.warn {
                                "Localization template '${localizationKey}' could not be found in bundle '${baseName}' with locale '${locale}' or below"
                            }
                        }
                    }
                } else {
                    SingleLogger.current().tryLog(baseName, locale.toLanguageTag()) {
                        logger.warn { "Localization bundle '${baseName}' with locale '${locale}' was specified to be valid but was not found." }
                    }
                }
            }
        }

        return map
    }

    private fun Locale.toDiscordLocale() = DiscordLocale.from(this)
}