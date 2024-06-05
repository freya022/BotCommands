package io.github.freya022.botcommands.api.localization

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

/**
 * Allows you to configure the localization settings of this interaction/command,
 * as well as retrieve a localization context from it.
 *
 * ### Configuring localization bundle and prefix
 * You can change the bundle and prefix in the first lines of your interaction handler,
 * with [localizationBundle] and [localizationPrefix].
 */
interface LocalizableAction {
    /**
     * If set, forces the specified localization bundle to be used.
     *
     * If unset, all bundles registered in [BLocalizationConfig.responseBundles] are used.
     */
    var localizationBundle: String?
    /**
     * If set, adds the specified prefix to the path of every localization call,
     * useful to avoid using very long strings in every reply/edit.
     *
     * For example, if you set this to `commands.ban.responses`, and try to localize with the path `missing_permissions`,
     * the final path will be `commands.ban.responses.missing_permissions`.
     */
    var localizationPrefix: String?

    fun getLocalizedMessage(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String

    fun getLocalizedMessage(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): String =
        getLocalizedMessage(locale.toLocale(), localizationPath, *entries)
}

fun LocalizableAction.getLocalizedMessage(locale: Locale, localizationPath: String, vararg entries: PairEntry): String =
    getLocalizedMessage(locale, localizationPath, *entries.mapToEntries())

fun LocalizableAction.getLocalizedMessage(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry): String =
    getLocalizedMessage(locale, localizationPath, *entries.mapToEntries())