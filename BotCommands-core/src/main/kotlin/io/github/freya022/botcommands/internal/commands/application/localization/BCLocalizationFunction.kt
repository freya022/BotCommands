package io.github.freya022.botcommands.internal.commands.application.localization

import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.enumMapOf
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.SingleLogger
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*

private val singleLogger = SingleLogger.of<BCLocalizationFunction>()

internal class BCLocalizationFunction(context: BContextImpl) : LocalizationFunction {
    private val logMissingLocalizationKeys = context.applicationConfig.logMissingLocalizationKeys
    private val localizationService: LocalizationService = context.getService<LocalizationService>()
    private val baseNameToLocalesMap: Map<String, List<DiscordLocale>> = context.applicationConfig.baseNameToLocalesMap

    override fun apply(localizationKey: String): Map<DiscordLocale, String> {
        val map: MutableMap<DiscordLocale, String> = enumMapOf()

        forEachBundle { baseName, javaLocale, discordLocale, localization ->
            val template = localization[localizationKey]
            if (template != null) {
                map[discordLocale] = template.localize()
            } else if (logMissingLocalizationKeys) {
                singleLogger.warn(baseName, discordLocale, localizationKey) {
                    "Localization template '${localizationKey}' could not be found in bundle '${baseName}' with Java locale '${javaLocale}' (from Discord's $discordLocale) or below"
                }
            }
        }

        return map
    }

    private fun forEachBundle(block: (baseName: String, javaLocale: Locale, discordLocale: DiscordLocale, localization: Localization) -> Unit) {
        baseNameToLocalesMap.forEach { (baseName, discordLocales) ->
            discordLocales.forEach localeLoop@{ discordLocale ->
                val javaLocale = discordLocale.toLocale()
                val instance = localizationService.getInstance(baseName, javaLocale)
                    ?: return@localeLoop singleLogger.warn(baseName, discordLocale) {
                        "Localization bundle '${baseName}' with Java locale '${javaLocale}' (from Discord's $discordLocale) was specified to be valid but was not found at all."
                    }

                if (instance.effectiveLocale !== javaLocale) {
                    singleLogger.warn(baseName, discordLocale, instance.effectiveLocale.toLanguageTag()) {
                        "Localization bundle '${baseName}' with Java locale '${javaLocale}' (from Discord's $discordLocale) was specified to be valid but was not found, falling back to '${instance.effectiveLocale}'"
                    }
                }

                block(baseName, javaLocale, discordLocale, instance)
            }
        }
    }
}