package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import net.dv8tion.jda.api.interactions.DiscordLocale

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
 */
interface LocalizableInteraction {
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

    fun getHook(): LocalizableInteractionHook

    fun getUserLocale(): DiscordLocale
    fun getGuildLocale(): DiscordLocale

    fun getLocalizationContext(bundleName: String): AppLocalizationContext =
        getLocalizationContext(bundleName, localizationPrefix = null)

    fun getLocalizationContext(bundleName: String, localizationPrefix: String?): AppLocalizationContext
}