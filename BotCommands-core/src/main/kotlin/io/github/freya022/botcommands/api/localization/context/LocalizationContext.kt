package io.github.freya022.botcommands.api.localization.context

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.LocalizationContext.Companion.builder
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.localization.LocalizationContextImpl
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import java.util.*
import java.util.function.Function
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
 * @see builder
 */
interface LocalizationContext {
    /**
     * The locale used when no locale is specified, the best locale is picked in this order:
     * - The [user locale][Interaction.getUserLocale]
     * - The [guild locale][Guild.getLocale]
     * - The default locale ([Locale.US])
     */
    val effectiveLocale: Locale

    /**
     * Returns the localization bundle of the current context.
     *
     * The localization bundle can either come from [LocalizationBundle.value] or [withBundle].
     *
     * @return The localization bundle for this context
     *
     * @see withBundle
     */
    val localizationBundle: String

    /**
     * The prefix to add to every localization request.
     *
     * This will be ignored if the request's path starts with a `/`.
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
    fun withGuildLocale(guildLocale: Locale?): TextLocalizationContext

    /**
     * Returns a new [AppLocalizationContext] with the specified user locale.
     *
     * @param userLocale The user locale to use, or `null` to remove it
     */
    @CheckReturnValue
    fun withUserLocale(userLocale: Locale?): AppLocalizationContext

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
     * Returns a new localization context with the specified localization bundle,
     * and resets the localization prefix.
     *
     * @param localizationBundle The localization bundle to use
     */
    @CheckReturnValue
    fun switchBundle(localizationBundle: String): LocalizationContext

    /**
     * Localizes the provided path, with the provided locale.
     *
     * @param locale           The [Locale] to use when fetching the localization bundle
     * @param localizationPath The path of the localization template,
     * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
     * @param entries          The entries to fill the template with
     */
    fun localize(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String

    /**
     * Localizes the provided path, with the provided locale, or returns `null` if the path does not exist.
     *
     * @param locale           The [Locale] to use when fetching the localization bundle
     * @param localizationPath The path of the localization template,
     * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
     * @param entries          The entries to fill the template with
     */
    fun localizeOrNull(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String?

    /**
     * Localizes the provided path, with the [best locale][effectiveLocale] available.
     *
     * @param localizationPath The path of the localization template,
     * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
     * @param entries          The entries to fill the template with
     */
    fun localize(localizationPath: String, vararg entries: Localization.Entry): String =
        localize(effectiveLocale, localizationPath, *entries)

    /**
     * Localizes the provided path, with the [best locale][effectiveLocale] available,
     * or returns `null` if the path does not exist.
     *
     * @param localizationPath The path of the localization template,
     * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
     * @param entries          The entries to fill the template with
     */
    fun localizeOrNull(localizationPath: String, vararg entries: Localization.Entry): String? =
        localizeOrNull(effectiveLocale, localizationPath, *entries)

    class Builder internal constructor(private val localizationService: LocalizationService, private val bundleName: String) {
        private var prefix: String? = null
        private var guildLocaleProvider: Lazy<Locale>? = null
        private var userLocaleProvider: Lazy<Locale>? = null

        /**
         * Sets the prefix of the context.
         *
         * @see LocalizationContext.localizationPrefix
         */
        fun setPrefix(prefix: String?): Builder {
            this.prefix = prefix
            return this
        }

        /**
         * Sets the guild locale to be provided by the passed [GuildLocaleProvider].
         */
        fun setGuildLocaleProvider(provider: GuildLocaleProvider, interaction: Interaction): Builder {
            return setGuildLocaleProvider(lazy { provider.getLocale(interaction) })
        }

        /**
         * Sets the guild locale to the provider function.
         *
         * This is supposed to be used as such:
         * ```java
         * TextCommandLocaleProvider localeProvider; // Assuming you have one
         *
         * setGuildLocaleProvider(localeProvider::getLocale, event)
         * ```
         */
        fun setGuildLocaleProvider(provider: Function<MessageReceivedEvent, Locale>, event: MessageReceivedEvent): Builder {
            return setGuildLocaleProvider(lazy { provider.apply(event) })
        }

        /**
         * Sets the guild locale to the provided one.
         */
        fun setGuildLocale(locale: Locale): Builder {
            return setGuildLocaleProvider(lazyOf(locale))
        }

        private fun setGuildLocaleProvider(provider: Lazy<Locale>): Builder {
            this.guildLocaleProvider = provider
            return this
        }

        /**
         * Sets the user locale to be provided by the passed [UserLocaleProvider].
         */
        fun setUserLocaleProvider(provider: UserLocaleProvider, interaction: Interaction): Builder {
            return setUserLocaleProvider(lazy { provider.getLocale(interaction) })
        }

        /**
         * Sets the user locale to the provided one.
         */
        fun setUserLocale(locale: Locale): Builder {
            return setUserLocaleProvider(lazyOf(locale))
        }

        private fun setUserLocaleProvider(provider: Lazy<Locale>): Builder {
            this.userLocaleProvider = provider
            return this
        }

        /**
         * Builds an instance with the current configuration.
         *
         * **Note:** This returns an [AppLocalizationContext] (instead of a [LocalizationContext]) to give you full capabilities.
         */
        fun build(): AppLocalizationContext {
            return LocalizationContextImpl(localizationService, bundleName, prefix, guildLocaleProvider, userLocaleProvider)
        }
    }

    companion object {
        /**
         * Creates a new builder, using a [LocalizationService] retrieved from the provided context,
         * and the specified bundle name, from which the strings will be retrieved from.
         */
        @JvmStatic
        fun builder(context: BContext, localizationBundle: String): Builder {
            return Builder(localizationService = context.getService(), localizationBundle)
        }

        /**
         * Creates a new builder, using a [LocalizationService] and the specified bundle name,
         * from which the strings will be retrieved from.
         */
        @JvmStatic
        fun builder(localizationService: LocalizationService, localizationBundle: String): Builder {
            return Builder(localizationService, localizationBundle)
        }
    }
}

// TODO shared internal
/** **INTERNAL** */
@JvmSynthetic
fun Array<out PairEntry>.mapToEntries() = Array(this.size) {
    Localization.Entry(this[it].first, this[it].second)
}

/**
 * Localizes the provided path, with the provided locale.
 *
 * @param locale           The [Locale] to use when fetching the localization bundle
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 */
fun LocalizationContext.localize(locale: Locale, localizationPath: String, vararg entries: PairEntry): String =
    localize(locale, localizationPath, *entries.mapToEntries())

/**
 * Localizes the provided path, with the provided locale, or returns `null` if the path does not exist.
 *
 * @param locale           The [Locale] to use when fetching the localization bundle
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 */
fun LocalizationContext.localizeOrNull(locale: Locale, localizationPath: String, vararg entries: PairEntry): String? =
    localizeOrNull(locale, localizationPath, *entries.mapToEntries())

/**
 * Localizes the provided path, with the [best locale][LocalizationContext.effectiveLocale] available.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 */
fun LocalizationContext.localize(localizationPath: String, vararg entries: PairEntry): String =
    localize(effectiveLocale, localizationPath, *entries.mapToEntries())

/**
 * Localizes the provided path, with the [best locale][LocalizationContext.effectiveLocale] available,
 * or returns `null` if the path does not exist.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 */
fun LocalizationContext.localizeOrNull(localizationPath: String, vararg entries: PairEntry): String? =
    localizeOrNull(effectiveLocale, localizationPath, *entries.mapToEntries())

//region Localized responses
/**
 * Sends a localized message to this channel.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 * 
 * @see MessageChannel.sendMessage
 */
fun MessageChannel.sendLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): MessageCreateAction =
    sendMessage(context.localize(localizationPath, *entries)).useComponentsV2(false)

/**
 * Replies a localized message to this interaction and acknowledges it.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see IReplyCallback.reply
 */
fun IReplyCallback.replyLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): ReplyCallbackAction =
    reply(context.localize(localizationPath, *entries)).useComponentsV2(false)

/**
 * Replies a localized ephemeral message to this interaction and acknowledges it.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see IReplyCallback.reply
 */
fun IReplyCallback.replyLocalizedEphemeral(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): ReplyCallbackAction =
    reply(context.localize(localizationPath, *entries)).useComponentsV2(false).setEphemeral(true)

/**
 * Sends a localized followup message to this interaction hook.
 *
 * If the interaction was originally [deferred][IReplyCallback.deferReply],
 * then the ephemeral-ness of this message depends on what was passed there.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see InteractionHook.sendMessage
 */
fun InteractionHook.sendLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): WebhookMessageCreateAction<Message> =
    sendMessage(context.localize(localizationPath, *entries)).useComponentsV2(false)

/**
 * Sends a localized ephemeral followup message to this interaction hook.
 *
 * If the interaction was originally [deferred][IReplyCallback.deferReply],
 * then the ephemeral-ness of this message depends on what was passed there.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see InteractionHook.sendMessage
 */
fun InteractionHook.sendLocalizedEphemeral(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): WebhookMessageCreateAction<Message> =
    sendMessage(context.localize(localizationPath, *entries)).useComponentsV2(false).setEphemeral(true)
//endregion

//region Localized edits
/**
 * Edits the text content of the original message with a localized message.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see IMessageEditCallback.editMessage
 */
fun IMessageEditCallback.editLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): MessageEditCallbackAction =
    editMessage(context.localize(localizationPath, *entries)).useComponentsV2(false)

/**
 * Replaces the entire original message with a localized message.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see IMessageEditCallback.editMessage
 */
fun IMessageEditCallback.replaceLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): MessageEditCallbackAction =
    editMessage(context.localize(localizationPath, *entries)).useComponentsV2(false).setReplace(true)

/**
 * Edits the text content of the original message with a localized message.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see InteractionHook.editOriginal
 */
fun InteractionHook.editLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): WebhookMessageEditAction<Message> =
    editOriginal(context.localize(localizationPath, *entries)).useComponentsV2(false)

/**
 * Replaces the entire original message with a localized message.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see InteractionHook.editOriginal
 */
fun InteractionHook.replaceLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): WebhookMessageEditAction<Message> =
    editOriginal(context.localize(localizationPath, *entries)).useComponentsV2(false).setReplace(true)

/**
 * Edits the text content of the message with a localized message.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see Message.editMessage
 */
fun Message.editLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): MessageEditAction =
    editMessage(context.localize(localizationPath, *entries)).useComponentsV2(false)

/**
 * Replaces the entire message with a localized message.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see Message.editMessage
 */
fun Message.replaceLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): MessageEditAction =
    editMessage(context.localize(localizationPath, *entries)).useComponentsV2(false).setReplace(true)
//endregion
