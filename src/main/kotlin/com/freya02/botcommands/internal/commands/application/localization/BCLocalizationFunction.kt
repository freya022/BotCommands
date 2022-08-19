package com.freya02.botcommands.internal.commands.application.localization

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.builder.DebugBuilder
import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*

class BCLocalizationFunction(context: BContextImpl) : LocalizationFunction {
    private val baseNameToLocalesMap: Map<String, List<Locale>> = context.config.applicationConfig.baseNameToLocalesMap

    override fun apply(localizationKey: String): Map<DiscordLocale, String> {
        val map: MutableMap<DiscordLocale, String> = EnumMap(DiscordLocale::class.java)

        baseNameToLocalesMap.forEach { (baseName, locales) ->
            for (locale in locales) {
                val instance = Localization.getInstance(baseName, locale)
                if (instance != null) {
                    if (instance.effectiveLocale !== locale) {
                        if (Logging.tryLog(baseName, locale.toLanguageTag(), instance.effectiveLocale.toLanguageTag())) {
                            logger.warn(
                                "Localization bundle '{}' with locale '{}' was specified to be valid but was not found, falling back to '{}'",
                                baseName,
                                locale,
                                instance.effectiveLocale
                            )
                        }
                    }

                    val template = instance[localizationKey]
                    if (template != null) {
                        map[DiscordLocale.from(locale)] = template.localize()
                    } else if (DebugBuilder.isLogMissingLocalizationEnabled()) {
                        if (Logging.tryLog(baseName, locale.toLanguageTag(), localizationKey)) {
                            logger.warn(
                                "Localization template '{}' could not be found in bundle '{}' with locale '{}' or below",
                                localizationKey,
                                baseName,
                                locale
                            )
                        }
                    }
                } else if (Logging.tryLog(baseName, locale.toLanguageTag())) {
                    logger.warn("Localization bundle '{}' with locale '{}' was specified to be valid but was not found.", baseName, locale)
                }
            }
        }

        return map
    }

    companion object {
        private val logger = Logging.getLogger()
    }
}