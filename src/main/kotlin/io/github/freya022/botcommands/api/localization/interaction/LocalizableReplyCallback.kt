package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import java.util.*

/**
 * Allows replying to interactions using localized strings,
 * registered from bundles in [BLocalizationConfig.responseBundles].
 *
 * See [LocalizableInteraction] for further configuration.
 *
 * @see BLocalizationConfig.responseBundles
 */
interface LocalizableReplyCallback {
    fun getHook(): LocalizableInteractionHook

    fun getUserLocale(): DiscordLocale
    fun getGuildLocale(): DiscordLocale

    fun replyUser(localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction =
        replyLocalized(getUserLocale(), localizationPath, *entries)

    fun replyGuild(localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction =
        replyLocalized(getGuildLocale(), localizationPath, *entries)

    fun replyLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction =
        replyLocalized(locale.toLocale(), localizationPath, *entries)

    fun replyLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction
}

fun LocalizableReplyCallback.replyUser(localizationPath: String, vararg entries: PairEntry) =
    replyUser(localizationPath, *entries.mapToEntries())

fun LocalizableReplyCallback.replyGuild(localizationPath: String, vararg entries: PairEntry) =
    replyGuild(localizationPath, *entries.mapToEntries())

fun LocalizableReplyCallback.replyLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry) =
    replyLocalized(locale, localizationPath, *entries.mapToEntries())

fun LocalizableReplyCallback.replyLocalized(locale: Locale, localizationPath: String, vararg entries: PairEntry) =
    replyLocalized(locale, localizationPath, *entries.mapToEntries())