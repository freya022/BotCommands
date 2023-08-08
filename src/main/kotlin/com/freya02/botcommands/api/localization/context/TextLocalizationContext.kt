package com.freya02.botcommands.api.localization.context

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import javax.annotation.CheckReturnValue

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes, and context-aware localization.
 *
 * This interface also includes the guild's locale by default if the guild is a community.
 *
 * While instances of this interface are primarily injected with [LocalizationBundle],
 * you can also construct instances of this interface with [LocalizationContext.create].
 *
 * Instances are only injectable if the event is a subclass of either [Interaction] or [MessageReceivedEvent].
 *
 * @see guildLocale
 * @see AppLocalizationContext
 *
 * @see LocalizationContext.create
 */
interface TextLocalizationContext : LocalizationContext {
    /**
     * Whether this localization context has a Guild locale.
     *
     * The locale can either come from the Guild or from [withGuildLocale].
     *
     * @return `true` if there is a guild locale in this context
     *
     * @see withGuildLocale
     * @see guildLocale
     */
    fun hasGuildLocale(): Boolean

    /**
     * Returns the [DiscordLocale] of the guild.
     *
     * The locale can either come from the Guild or from a [withGuildLocale].
     *
     * **Note:** If the context does not provide a guild locale (such as text commands) but the event comes from a [Guild] and is a community,
     * then the community's locale will be returned, or [DiscordLocale.ENGLISH_US] by default.
     *
     * @return the [DiscordLocale] of the guild
     *
     * @throws IllegalStateException If the event did not happen in a Guild and the guild locale was not supplied
     *
     * @see hasGuildLocale
     * @see withGuildLocale
     */
    val guildLocale: DiscordLocale

    @CheckReturnValue
    override fun withBundle(localizationBundle: String): TextLocalizationContext

    @CheckReturnValue
    override fun withPrefix(localizationPrefix: String?): TextLocalizationContext

    /**
     * Localizes the provided path, with the guild's locale.
     *
     * This will localize to `en_US` if the Guild does not have the `COMMUNITY` feature flag.
     *
     * @param localizationPath The localization path to search for
     * @param entries          The entries to fill the template
     *
     * @throws IllegalStateException If the event did not happen in a Guild
     *
     * @see Guild.getLocale
     */
    fun localizeGuild(localizationPath: String, vararg entries: Localization.Entry): String =
        localize(guildLocale, localizationPath, *entries)

    /**
     * Localizes the provided path, with the guild's locale.
     *
     * This will localize to `en_US` if the Guild does not have the `COMMUNITY` feature flag.
     *
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @throws IllegalStateException If the event did not happen in a Guild
     *
     * @see Guild.getLocale
     */
    @JvmSynthetic
    fun localizeGuild(localizationPath: String, vararg entries: PairEntry): String =
        localize(guildLocale, localizationPath, *entries.mapToEntries())
}
