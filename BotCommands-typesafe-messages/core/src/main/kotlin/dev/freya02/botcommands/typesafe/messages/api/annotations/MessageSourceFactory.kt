package dev.freya02.botcommands.typesafe.messages.api.annotations

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.springframework.stereotype.Component

/**
 * Mandatory annotation for interfaces extending [IMessageSourceFactory].
 *
 * @see bundleName
 */
@Component
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class MessageSourceFactory(
    /**
     * The name of the bundle to get the templates from.
     *
     * The bundles will be searched using the accessible [localization map providers][LocalizationMapProvider].
     */
    val bundleName: String,

    /**
     * The [DiscordLocales][DiscordLocale] available for the corresponding bundle.
     * It is recommended to put all the locales supported, so the framework can check for errors early.
     *
     * This will be merged together with [locales].
     *
     * An exception will be thrown if one of them isn't available.
     */
    val discordLocales: Array<DiscordLocale> = [],

    /**
     * The [locale language tags][java.util.Locale.forLanguageTag] available for the corresponding bundle.
     * It is recommended to put all the locales supported, so the framework can check for errors early.
     *
     * This will be merged together with [discordLocales].
     *
     * An exception will be thrown if one of them isn't available.
     */
    val locales: Array<String> = [],

    /**
     * Whether to ignore when [discordLocales] and [locales] are empty.
     * This should be set to `true` only if you use this as a non-localized message source.
     */
    val ignoreEmptyLocales: Boolean = false,
)
