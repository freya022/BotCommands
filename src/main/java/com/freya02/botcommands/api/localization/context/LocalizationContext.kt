package com.freya02.botcommands.api.localization.context

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.LocalizationService
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.LocalizationContext.Companion.create
import com.freya02.botcommands.internal.localization.LocalizationContextImpl
import net.dv8tion.jda.api.interactions.DiscordLocale
import javax.annotation.CheckReturnValue

typealias PairEntry = Pair<String, Any>

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes, and context-aware localization.
 *
 * While this interface cannot be injected, sub-interfaces can.
 *
 * @see TextLocalizationContext
 * @see AppLocalizationContext
 * @see create
 */
interface LocalizationContext {
    val effectiveLocale: DiscordLocale

    /**
     * Returns the localization bundle of the current context.
     *
     * The localization bundle can either come from [LocalizationBundle.value] from [withBundle].
     *
     * @return The localization bundle for this context
     *
     * @see withBundle
     */
    val localizationBundle: String

    /**
     * Returns the localization prefix of the current context.
     *
     * The localization prefix can either come from [LocalizationBundle.prefix] from [withPrefix].
     *
     * @return The localization prefix for this context, or `null` if none has been set
     *
     * @see withPrefix
     */
    val localizationPrefix: String?

    /**
     * Returns a new [TextLocalizationContext] with the specified guild locale.
     *
     * @param guildLocale The guild locale to use, or `null` to remove it
     */
    @CheckReturnValue
    fun withGuildLocale(guildLocale: DiscordLocale?): TextLocalizationContext

    /**
     * Returns a new [AppLocalizationContext] with the specified user locale.
     *
     * @param userLocale The user locale to use, or `null` to remove it
     */
    @CheckReturnValue
    fun withUserLocale(userLocale: DiscordLocale?): AppLocalizationContext

    /**
     * Returns a new localization context with the specified localization bundle.
     *
     * @param localizationBundle The localization bundle to use
     */
    @CheckReturnValue
    fun withBundle(localizationBundle: String): LocalizationContext

    /**
     * Returns a new localization context with the specified localization prefix.
     *
     * @param localizationPrefix The localization prefix to use, or `null` to remove it
     */
    @CheckReturnValue
    fun withPrefix(localizationPrefix: String?): LocalizationContext

    /**
     * Localizes the provided path, in the current context's bundle, with the provided locale.
     *
     * @param locale             The DiscordLocale to use when fetching the localization bundle
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     */
    fun localize(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): String

    /**
     * Localizes the provided path, in the specified bundle, with the provided locale.
     *
     * @param locale             The DiscordLocale to use when fetching the localization bundle
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     */
    @JvmSynthetic
    fun localize(locale: DiscordLocale, localizationPath: String, vararg entries: PairEntry): String =
        localize(locale, localizationPath, *entries.mapToEntries())

    /**
     * Localizes the provided path, in the specified bundle, with the provided locale.
     *
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     */
    @JvmSynthetic
    fun localize(localizationPath: String, vararg entries: PairEntry): String =
        localize(effectiveLocale, localizationPath, *entries.mapToEntries())

    /**
     * Localizes the provided path, in the current context's bundle, with the best locale available. (User > Guild > Default)
     *
     * @param localizationPath The localization path to search for
     * @param entries          The entries to fill the template
     */
    fun localize(localizationPath: String, vararg entries: Localization.Entry): String =
        localize(effectiveLocale, localizationPath, *entries)

    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(
            context: BContext,
            localizationBundle: String,
            localizationPrefix: String? = null,
            guildLocale: DiscordLocale? = null,
            userLocale: DiscordLocale? = null
        ): AppLocalizationContext {
            return LocalizationContextImpl(
                context.getService(LocalizationService::class.java),
                localizationBundle,
                localizationPrefix,
                guildLocale,
                userLocale
            )
        }
    }
}

@JvmSynthetic
internal fun Array<out PairEntry>.mapToEntries() = Array(this.size) {
    Localization.Entry(this[it].first, this[it].second)
}
