package io.github.freya022.botcommands.api.localization.text

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.LocalizableAction
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import java.util.*
import javax.annotation.CheckReturnValue

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
 *
 * @see Localization
 */
interface LocalizableTextCommand : LocalizableAction {
    /**
     * Returns the localized message at the following [path][localizationPath],
     * using the guild's locale and provided parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    fun getGuildMessage(localizationPath: String, vararg entries: Localization.Entry): String

    /**
     * Responds with the localized message at the following [path][localizationPath],
     * using the guild's locale and provided parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun respondGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction

    /**
     * Replies with the localized message at the following [path][localizationPath],
     * using the guild's locale and provided parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun replyGuild(localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction

    /**
     * Responds with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun respondLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction =
        respondLocalized(locale.toLocale(), localizationPath, *entries)

    /**
     * Replies with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun replyLocalized(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction =
        replyLocalized(locale.toLocale(), localizationPath, *entries)

    /**
     * Responds with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun respondLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction

    /**
     * Replies with the localized message at the following [path][localizationPath],
     * using the provided locale and parameters.
     *
     * ### Bundle resolution
     * The bundle used is either the [defined bundle][localizationBundle]
     * or one of the [registered bundles][BLocalizationConfig.responseBundles].
     *
     * The locale of the bundle is the best available,
     * for example, if `fr_FR` is not available, then `fr` will be used,
     * and otherwise, the root bundle (without any suffix) will be used.
     *
     * @param localizationPath The path of the message to translate, will be prefixed with [localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun replyLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction
}

fun LocalizableTextCommand.getGuildMessage(localizationPath: String, vararg entries: PairEntry): String =
    getGuildMessage(localizationPath, *entries.mapToEntries())

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