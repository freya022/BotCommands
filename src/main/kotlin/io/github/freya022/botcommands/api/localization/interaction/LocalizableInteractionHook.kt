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
import javax.annotation.CheckReturnValue

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
    /**
     * The localizable interaction attached to this hook.
     */
    val localizableInteraction: LocalizableInteraction

    /**
     * Sends a follow-up with the localized message at the following [path][localizationPath],
     * using the user's locale and provided parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun sendUser(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message>

    /**
     * Sends a follow-up with the localized message at the following [path][localizationPath],
     * using the guild's locale and provided parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun sendGuild(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message>

    /**
     * Sends a follow-up with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun sendLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message> =
        sendLocalized(locale.toLocale(), localizationPath, *entries)

    /**
     * Sends a follow-up with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun sendLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message>

    /**
     * Edits the original message with the localized message at the following [path][localizationPath],
     * using the user's locale and provided parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun editUser(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message>

    /**
     * Edits the original message with the localized message at the following [path][localizationPath],
     * using the guild's locale and provided parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun editGuild(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message>

    /**
     * Edits the original message with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun editLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message> =
        editLocalized(locale.toLocale(), localizationPath, *entries)

    /**
     * Edits the original message with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][LocalizableInteraction.localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [LocalizableInteraction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
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