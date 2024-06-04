package io.github.freya022.botcommands.api.localization.text

import io.github.freya022.botcommands.api.localization.LocalizableAction
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import java.util.*

/**
 * Allows you to configure the localization settings of this text command event,
 * as well as retrieve a localization context from it.
 *
 * ### Configuring localization bundle and prefix
 * You can change the bundle and prefix in the first lines of your interaction handler,
 * with [localizationBundle] and [localizationPrefix].
 *
 * ### Configuring the user / guild locale source
 * They are by default retrieved from the interaction,
 * but you can get them in other ways by implementing [UserLocaleProvider] and/or [GuildLocaleProvider].
 */
interface LocalizableTextCommand : LocalizableAction {
    fun respondGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction

    fun replyGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction

    fun respondLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction =
        respondLocalized(locale.toLocale(), localizationPath, *entries)

    fun replyLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction =
        replyLocalized(locale.toLocale(), localizationPath, *entries)

    fun respondLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction

    fun replyLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction
}

fun LocalizableTextCommand.respondGuild(localizationPath: String, vararg entries: PairEntry) =
    respondGuild(localizationPath, *entries.mapToEntries())

fun LocalizableTextCommand.replyGuild(localizationPath: String, vararg entries: PairEntry) =
    replyGuild(localizationPath, *entries.mapToEntries())

fun LocalizableTextCommand.respondLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry) =
    respondLocalized(locale, localizationPath, *entries.mapToEntries())

fun LocalizableTextCommand.replyLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry) =
    replyLocalized(locale, localizationPath, *entries.mapToEntries())

fun LocalizableTextCommand.respondLocalized(locale: Locale, localizationPath: String, vararg entries: PairEntry) =
    respondLocalized(locale, localizationPath, *entries.mapToEntries())

fun LocalizableTextCommand.replyLocalized(locale: Locale, localizationPath: String, vararg entries: PairEntry) =
    replyLocalized(locale, localizationPath, *entries.mapToEntries())