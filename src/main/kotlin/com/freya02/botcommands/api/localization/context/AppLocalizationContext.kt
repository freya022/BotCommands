package com.freya02.botcommands.api.localization.context

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import javax.annotation.CheckReturnValue

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes, and context-aware localization.
 *
 * This interface also includes the user's and the guild's locale by default.
 *
 * While instances of this interface are primarily injected with [LocalizationBundle],
 * you can also construct instances of this interface with [LocalizationContext.create].
 *
 * Instances are only injectable if the event is a subclass of either [Interaction].
 *
 * @see userLocale
 * @see guildLocale
 * @see LocalizationContext.create
 */
interface AppLocalizationContext : TextLocalizationContext {
    /**
     * Returns the Locale of the user.
     *
     * The locale can either come from the [Interaction] or from [LocalizationContext.withGuildLocale].
     *
     * @return The Locale of the user
     *
     * @see withUserLocale
     */
    //User locale is always provided in interactions
    val userLocale: DiscordLocale

    @CheckReturnValue
    override fun withGuildLocale(guildLocale: DiscordLocale?): AppLocalizationContext

    @CheckReturnValue
    override fun withBundle(localizationBundle: String): AppLocalizationContext

    @CheckReturnValue
    override fun withPrefix(localizationPrefix: String?): AppLocalizationContext

    @CheckReturnValue
    override fun switchBundle(localizationBundle: String): AppLocalizationContext

    /**
     * Localizes the provided path, with the user's locale.
     *
     * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
     * @param entries          The entries to fill the template with
     *
     * @return The localized string
     */
    fun localizeUser(localizationPath: String, vararg entries: Localization.Entry): String =
        localize(userLocale, localizationPath, *entries)

    /**
     * Localizes the provided path, with the user's locale, or returns `null` if the path does not exist.
     *
     * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
     * @param entries          The entries to fill the template with
     *
     * @return The localized string
     */
    fun localizeUserOrNull(localizationPath: String, vararg entries: Localization.Entry): String? =
        localizeOrNull(userLocale, localizationPath, *entries)
}

/**
 * Localizes the provided path, with the user's locale.
 *
 * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
 * @param entries          The entries to fill the template with
 *
 * @return The localized string
 */
fun AppLocalizationContext.localizeUser(localizationPath: String, vararg entries: PairEntry): String =
    localize(userLocale, localizationPath, *entries.mapToEntries())

/**
 * Localizes the provided path, with the user's locale, or returns `null` if the path does not exist.
 *
 * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
 * @param entries          The entries to fill the template with
 *
 * @return The localized string
 */
fun AppLocalizationContext.localizeUserOrNull(localizationPath: String, vararg entries: PairEntry): String? =
    localizeOrNull(userLocale, localizationPath, *entries.mapToEntries())
