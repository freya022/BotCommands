package io.github.freya022.botcommands.api.core.replies

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Default implementation of [BuiltinRepliesFactory].
 *
 * Instances returned by this factory are [DefaultBuiltinReplies],
 * which uses [LocalizationService] to get a localization bundle using the provided [bundleName].
 *
 * By default, an instance of this factory is registered using the default bundle name (`BuiltinReplies`),
 * you can override the default factory by providing a corresponding service factory.
 *
 * ### Light customization / Supporting more locales
 *
 * With the default values, the localization templates would be loaded from `/bc_localization/BuiltinReplies-default.json`,
 * you may change the values by creating a new `BuiltinReplies.json`,
 * you can also support more locales following by appending an underscore and the [language tag][Locale.toLanguageTag],
 * such as `BuiltinReplies_fr.json`.
 *
 * The localization paths must be identical to those used by [DefaultBuiltinReplies],
 * but the placeholders can be modified but must keep the same names, they can also be removed.
 *
 * Refer to [Localization] for mode customization details.
 *
 * @see Localization
 */
class DefaultBuiltinRepliesFactory(
    private val localizationService: LocalizationService,
    private val textCommandLocaleProvider: TextCommandLocaleProvider,
    private val userLocaleProvider: UserLocaleProvider,
    private val bundleName: String = "BuiltinReplies",
) : BuiltinRepliesFactory {

    private val cache: MutableMap<Locale, BuiltinReplies> = hashMapOf()
    private val lock = ReentrantLock()

    override fun get(locale: Locale): BuiltinReplies {
        cache[locale]?.let { return it }

        return lock.withLock {
            cache.getOrPut(locale) { DefaultBuiltinReplies(localizationService, locale, bundleName) }
        }
    }

    override fun get(event: MessageReceivedEvent): BuiltinReplies = get(textCommandLocaleProvider.getLocale(event))

    override fun get(event: Interaction): BuiltinReplies = get(userLocaleProvider.getLocale(event))
}