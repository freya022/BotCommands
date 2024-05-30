package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import java.util.*

/**
 * Allows sending follow-ups to interactions using localized strings,
 * registered from bundles in [BLocalizationConfig.responseBundles].
 *
 * See [LocalizableInteraction] for further configuration.
 *
 * @see BLocalizationConfig.responseBundles
 * @see InteractionHook
 */
interface LocalizableInteractionHook : InteractionHook {
    fun sendUser(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message>

    fun sendGuild(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message>

    fun sendLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message> =
        sendLocalized(locale.toLocale(), localizationPath, *entries)

    fun sendLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message>

    fun editUser(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message>

    fun editGuild(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message>

    fun editLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message> =
        editLocalized(locale.toLocale(), localizationPath, *entries)

    fun editLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message>
}

fun LocalizableInteractionHook.sendUser(localizationPath: String, vararg entries: PairEntry) =
    sendUser(localizationPath, *entries.mapToEntries())

fun LocalizableInteractionHook.sendGuild(localizationPath: String, vararg entries: PairEntry) =
    sendGuild(localizationPath, *entries.mapToEntries())

fun LocalizableInteractionHook.sendLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry) =
    sendLocalized(locale, localizationPath, *entries.mapToEntries())

fun LocalizableInteractionHook.editUser(localizationPath: String, vararg entries: PairEntry) =
    editUser(localizationPath, *entries.mapToEntries())

fun LocalizableInteractionHook.editGuild(localizationPath: String, vararg entries: PairEntry) =
    editGuild(localizationPath, *entries.mapToEntries())

fun LocalizableInteractionHook.editLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry) =
    editLocalized(locale, localizationPath, *entries.mapToEntries())