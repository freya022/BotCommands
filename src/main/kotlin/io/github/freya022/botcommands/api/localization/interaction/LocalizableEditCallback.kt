package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import java.util.*

/**
 * Allows editing an interaction's original message using localized strings,
 * registered from bundles in [BLocalizationConfig.responseBundles].
 *
 * See [LocalizableInteraction] for further configuration.
 *
 * @see BLocalizationConfig.responseBundles
 */
interface LocalizableEditCallback {
    fun getHook(): LocalizableInteractionHook

    fun getUserLocale(): DiscordLocale
    fun getGuildLocale(): DiscordLocale

    fun editUser(localizationPath: String, vararg entries: Localization.Entry): MessageEditCallbackAction =
        editLocalized(getUserLocale(), localizationPath, *entries)

    fun editGuild(localizationPath: String, vararg entries: Localization.Entry): MessageEditCallbackAction =
        editLocalized(getGuildLocale(), localizationPath, *entries)

    fun editLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): MessageEditCallbackAction =
        editLocalized(locale.toLocale(), localizationPath, *entries)

    fun editLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): MessageEditCallbackAction
}

fun LocalizableEditCallback.editUser(localizationPath: String, vararg entries: PairEntry) =
    editUser(localizationPath, *entries.mapToEntries())

fun LocalizableEditCallback.editGuild(localizationPath: String, vararg entries: PairEntry) =
    editGuild(localizationPath, *entries.mapToEntries())

fun LocalizableEditCallback.editLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry) =
    editLocalized(locale, localizationPath, *entries.mapToEntries())

fun LocalizableEditCallback.editLocalized(locale: Locale, localizationPath: String, vararg entries: PairEntry) =
    editLocalized(locale, localizationPath, *entries.mapToEntries())