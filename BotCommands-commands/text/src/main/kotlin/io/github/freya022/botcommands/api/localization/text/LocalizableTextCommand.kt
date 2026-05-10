package io.github.freya022.botcommands.api.localization.text

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.LocalizableAction
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.localization.context.mapToEntries
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import java.util.Locale
import javax.annotation.CheckReturnValue

/**
 * Allows you to configure the localization settings of this text command event,
 * as well as retrieve a localization context from it.
 *
 * ### Configuring localization bundle and prefix
 * You can change the bundle and prefix in the first lines of your interaction handler,
 * with [localizationBundle] and [localizationPrefix].
 *
 * ### Configuring the guild locale source
 * They are by default retrieved from the guild,
 * but you can get them in other ways by implementing [MessageLocaleProvider].
 *
 * @see Localization
 */
interface LocalizableTextCommand : LocalizableAction {
    /**
     * Returns a localization context for the provided bundle name and path prefix,
     * using the locale from [MessageLocaleProvider].
     */
    override fun getLocalizationContext(bundleName: String, pathPrefix: String?): TextLocalizationContext

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
     * ### Configuring the guild locale source
     * The locale is retrieved from the [guild][Guild.getLocale] by default,
     * but can be changed by implementing your own [MessageLocaleProvider] service.
     *
     * @param localizationPath The path of the message to translate,
     * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
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
     * ### Configuring the guild locale source
     * The locale is retrieved from the [guild][Guild.getLocale] by default,
     * but can be changed by implementing your own [MessageLocaleProvider] service.
     *
     * @param localizationPath The path of the message to translate,
     * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
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
     * ### Configuring the guild locale source
     * The locale is retrieved from the [guild][Guild.getLocale] by default,
     * but can be changed by implementing your own [MessageLocaleProvider] service.
     *
     * @param localizationPath The path of the message to translate,
     * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
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
     * @param localizationPath The path of the message to translate,
     * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
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
     * @param localizationPath The path of the message to translate,
     * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    @CheckReturnValue
    fun replyLocalized(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): MessageCreateAction
}

/**
 * Returns the localized message at the following [path][localizationPath],
 * using the guild's locale and provided parameters.
 *
 * ### Bundle resolution
 * The bundle used is either the [defined bundle][LocalizableTextCommand.localizationBundle]
 * or one of the [registered bundles][BLocalizationConfig.responseBundles].
 *
 * The locale of the bundle is the best available,
 * for example, if `fr_FR` is not available, then `fr` will be used,
 * and otherwise, the root bundle (without any suffix) will be used.
 *
 * ### Configuring the guild locale source
 * The locale is retrieved from the [guild][Guild.getLocale] by default,
 * but can be changed by implementing your own [MessageLocaleProvider] service.
 *
 * @param localizationPath The path of the message to translate,
 * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
 * @param entries          The values replacing arguments of the localization template
 *
 * @throws IllegalArgumentException If:
 * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
 * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
 * - If the template requires an argument that was not passed to [entries]
 */
fun LocalizableTextCommand.getGuildMessage(localizationPath: String, vararg entries: PairEntry): String =
    getGuildMessage(localizationPath, *entries.mapToEntries())

/**
 * Responds with the localized message at the following [path][localizationPath],
 * using the guild's locale and provided parameters.
 *
 * ### Bundle resolution
 * The bundle used is either the [defined bundle][LocalizableTextCommand.localizationBundle]
 * or one of the [registered bundles][BLocalizationConfig.responseBundles].
 *
 * The locale of the bundle is the best available,
 * for example, if `fr_FR` is not available, then `fr` will be used,
 * and otherwise, the root bundle (without any suffix) will be used.
 *
 * ### Configuring the guild locale source
 * The locale is retrieved from the [guild][Guild.getLocale] by default,
 * but can be changed by implementing your own [MessageLocaleProvider] service.
 *
 * @param localizationPath The path of the message to translate,
 * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
 * @param entries          The values replacing arguments of the localization template
 *
 * @throws IllegalArgumentException If:
 * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
 * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
 * - If the template requires an argument that was not passed to [entries]
 */
fun LocalizableTextCommand.respondGuild(localizationPath: String, vararg entries: PairEntry) =
    respondGuild(localizationPath, *entries.mapToEntries())

/**
 * Replies with the localized message at the following [path][localizationPath],
 * using the guild's locale and provided parameters.
 *
 * ### Bundle resolution
 * The bundle used is either the [defined bundle][LocalizableTextCommand.localizationBundle]
 * or one of the [registered bundles][BLocalizationConfig.responseBundles].
 *
 * The locale of the bundle is the best available,
 * for example, if `fr_FR` is not available, then `fr` will be used,
 * and otherwise, the root bundle (without any suffix) will be used.
 *
 * ### Configuring the guild locale source
 * The locale is retrieved from the [guild][Guild.getLocale] by default,
 * but can be changed by implementing your own [MessageLocaleProvider] service.
 *
 * @param localizationPath The path of the message to translate,
 * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
 * @param entries          The values replacing arguments of the localization template
 *
 * @throws IllegalArgumentException If:
 * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
 * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
 * - If the template requires an argument that was not passed to [entries]
 */
fun LocalizableTextCommand.replyGuild(localizationPath: String, vararg entries: PairEntry) =
    replyGuild(localizationPath, *entries.mapToEntries())

/**
 * Responds with the localized message at the following [path][localizationPath],
 * using the provided locale and parameters.
 *
 * ### Bundle resolution
 * The bundle used is either the [defined bundle][LocalizableTextCommand.localizationBundle]
 * or one of the [registered bundles][BLocalizationConfig.responseBundles].
 *
 * The locale of the bundle is the best available,
 * for example, if `fr_FR` is not available, then `fr` will be used,
 * and otherwise, the root bundle (without any suffix) will be used.
 *
 * @param localizationPath The path of the message to translate,
 * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
 * @param entries          The values replacing arguments of the localization template
 *
 * @throws IllegalArgumentException If:
 * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
 * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
 * - If the template requires an argument that was not passed to [entries]
 */
fun LocalizableTextCommand.respondLocalized(locale: Locale, localizationPath: String, vararg entries: PairEntry) =
    respondLocalized(locale, localizationPath, *entries.mapToEntries())

/**
 * Replies with the localized message at the following [path][localizationPath],
 * using the provided locale and parameters.
 *
 * ### Bundle resolution
 * The bundle used is either the [defined bundle][LocalizableTextCommand.localizationBundle]
 * or one of the [registered bundles][BLocalizationConfig.responseBundles].
 *
 * The locale of the bundle is the best available,
 * for example, if `fr_FR` is not available, then `fr` will be used,
 * and otherwise, the root bundle (without any suffix) will be used.
 *
 * @param localizationPath The path of the message to translate,
 * will be prefixed with [localizationPrefix][LocalizableAction.localizationPrefix]
 * @param entries          The values replacing arguments of the localization template
 *
 * @throws IllegalArgumentException If:
 * - [localizationBundle][LocalizableAction.localizationBundle] is set, but the bundle doesn't exist
 * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
 * - If the template requires an argument that was not passed to [entries]
 */
fun LocalizableTextCommand.replyLocalized(locale: Locale, localizationPath: String, vararg entries: PairEntry) =
    replyLocalized(locale, localizationPath, *entries.mapToEntries())
