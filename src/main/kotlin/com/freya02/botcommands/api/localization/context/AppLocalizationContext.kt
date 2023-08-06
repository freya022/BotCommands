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

    /**
     * Localizes the provided path, in the current context's bundle, with the user's locale.
     *
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @return The localized string
     */
    @JvmSynthetic
    fun localizeUser(localizationPath: String, vararg entries: PairEntry): String =
        localize(userLocale, localizationPath, *entries.mapToEntries())

    /**
     * Localizes the provided path, in the current context's bundle, with the user's locale.
     *
     * @param localizationPath The localization path to search for
     * @param entries          The entries to fill the template
     *
     * @return The localized string
     */
    fun localizeUser(localizationPath: String, vararg entries: Localization.Entry): String =
        localize(userLocale, localizationPath, *entries)
}
