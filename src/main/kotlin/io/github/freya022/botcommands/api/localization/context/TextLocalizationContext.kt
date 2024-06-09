package io.github.freya022.botcommands.api.localization.context

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import javax.annotation.CheckReturnValue

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes, and context-aware localization.
 *
 * ### Guild locale
 * The guild locale is set by [GuildLocaleProvider] (by default [Guild.getLocale]).
 *
 * You can implement your own locale provider to change the locale used by this localization context.
 *
 * ### Manual usage
 * While instances of this interface are primarily injected with [@LocalizationBundle][LocalizationBundle],
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
     * The locale can either come from the [GuildLocaleProvider] or from a [withGuildLocale].
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

    @CheckReturnValue
    override fun switchBundle(localizationBundle: String): TextLocalizationContext

    /**
     * Localizes the provided path, with the guild's locale.
     *
     * This will localize to `en_US` if the Guild does not have the `COMMUNITY` feature flag.
     *
     * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
     * @param entries          The entries to fill the template with
     *
     * @throws IllegalStateException If the event did not happen in a Guild
     *
     * @see Guild.getLocale
     */
    fun localizeGuild(localizationPath: String, vararg entries: Localization.Entry): String =
        localize(guildLocale, localizationPath, *entries)

    /**
     * Localizes the provided path, with the guild's locale, or returns `null` if the path does not exist.
     *
     * This will localize to `en_US` if the Guild does not have the `COMMUNITY` feature flag.
     *
     * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
     * @param entries          The entries to fill the template with
     *
     * @throws IllegalStateException If the event did not happen in a Guild
     *
     * @see Guild.getLocale
     */
    fun localizeGuildOrNull(localizationPath: String, vararg entries: Localization.Entry): String? =
        localizeOrNull(guildLocale, localizationPath, *entries)
}

/**
 * Localizes the provided path, with the guild's locale.
 *
 * This will localize to `en_US` if the Guild does not have the `COMMUNITY` feature flag.
 *
 * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
 * @param entries          The entries to fill the template with
 *
 * @throws IllegalStateException If the event did not happen in a Guild
 *
 * @see Guild.getLocale
 */
fun TextLocalizationContext.localizeGuild(localizationPath: String, vararg entries: PairEntry): String =
    localize(guildLocale, localizationPath, *entries.mapToEntries())

/**
 * Localizes the provided path, with the guild's locale, or returns `null` if the path does not exist.
 *
 * This will localize to `en_US` if the Guild does not have the `COMMUNITY` feature flag.
 *
 * @param localizationPath The path of the localization template, prefixed with [localizationPrefix][LocalizationContext.localizationPrefix]
 * @param entries          The entries to fill the template with
 *
 * @throws IllegalStateException If the event did not happen in a Guild
 *
 * @see Guild.getLocale
 */
fun TextLocalizationContext.localizeGuildOrNull(localizationPath: String, vararg entries: PairEntry): String? =
    localizeOrNull(guildLocale, localizationPath, *entries.mapToEntries())