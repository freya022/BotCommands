package io.github.freya022.botcommands.api.core.messages

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Default implementation of [BotCommandsMessagesFactory].
 *
 * Instances returned by this factory are [DefaultBotCommandsMessages],
 * which uses [LocalizationService] to get a localization bundle using the provided [bundleName].
 *
 * By default, an instance of this factory is registered using the default bundle name (`BotCommandsMessages`),
 * you can override the default factory by providing a corresponding service factory.
 *
 * ### Light customization / Supporting more locales
 *
 * With the default values, the localization templates would be loaded from `/bc_localization/BotCommandsMessages-default.json`,
 * you may change the values by creating a new `BotCommandsMessages.json`,
 * you can also support more locales following by appending an underscore and the [language tag][Locale.toLanguageTag],
 * such as `BotCommandsMessages_fr.json`.
 *
 * The localization paths must be identical to those used by [DefaultBotCommandsMessages],
 * but the placeholders can be modified but must keep the same names, they can also be removed.
 *
 * Refer to [Localization] for mode customization details.
 *
 * @see Localization
 */
class DefaultBotCommandsMessagesFactory(
    private val permissionLocalization: PermissionLocalization,
    private val localizationService: LocalizationService,
    private val textCommandLocaleProvider: TextCommandLocaleProvider,
    private val userLocaleProvider: UserLocaleProvider,
    private val bundleName: String = "BotCommandsMessages",
) : BotCommandsMessagesFactory {

    private val cache: MutableMap<Locale, DefaultBotCommandsMessages> = hashMapOf()
    private val lock = ReentrantLock()

    override fun get(locale: Locale): DefaultBotCommandsMessages {
        cache[locale]?.let { return it }

        return lock.withLock {
            cache.getOrPut(locale) { DefaultBotCommandsMessages(permissionLocalization, localizationService, locale, bundleName) }
        }
    }

    override fun get(event: MessageReceivedEvent): DefaultBotCommandsMessages = get(textCommandLocaleProvider.getLocale(event))

    override fun get(event: Interaction): DefaultBotCommandsMessages = get(userLocaleProvider.getLocale(event))
}