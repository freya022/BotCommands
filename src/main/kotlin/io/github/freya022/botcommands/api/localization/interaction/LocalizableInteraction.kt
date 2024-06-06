package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.LocalizableAction
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries

/**
 * Allows you to configure the localization settings of this interaction event,
 * retrieve a [LocalizableInteractionHook],
 * as well as retrieve a localization context from it.
 *
 * Always combined with [LocalizableReplyCallback] and/or [LocalizableEditCallback].
 *
 * ### Configuring localization bundle and prefix
 * You can change the bundle and prefix in the first lines of your interaction handler,
 * with [localizationBundle] and [localizationPrefix].
 *
 * ### Configuring the user / guild locale source
 * They are by default retrieved from the interaction,
 * but you can get them in other ways by implementing [UserLocaleProvider] and/or [GuildLocaleProvider].
 *
 * @see LocalizableReplyCallback
 * @see LocalizableEditCallback
 * @see LocalizableInteractionHook
 *
 * @see Localization
 */
interface LocalizableInteraction : LocalizableAction {
    fun getHook(): LocalizableInteractionHook

    /**
     * Returns the localized message at the following [path][localizationPath],
     * using the user's locale and parameters.
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
     * will be prefixed with [localizationPrefix][LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    fun getUserMessage(localizationPath: String, vararg entries: Localization.Entry): String

    /**
     * Returns the localized message at the following [path][localizationPath],
     * using the guild's locale and parameters.
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
     * will be prefixed with [localizationPrefix][LocalizableInteraction.localizationPrefix]
     * @param entries          The values replacing arguments of the localization template
     *
     * @throws IllegalArgumentException If:
     * - [localizationBundle] is set, but the bundle doesn't exist
     * - No [registered bundle][BLocalizationConfig.responseBundles] containing the path could be found
     * - If the template requires an argument that was not passed to [entries]
     */
    fun getGuildMessage(localizationPath: String, vararg entries: Localization.Entry): String
}

fun LocalizableInteraction.getUserMessage(localizationPath: String, vararg entries: PairEntry): String =
    getUserMessage(localizationPath, *entries.mapToEntries())

fun LocalizableInteraction.getGuildMessage(localizationPath: String, vararg entries: PairEntry): String =
    getGuildMessage(localizationPath, *entries.mapToEntries())