package io.github.freya022.botcommands.api.commands.application.messages

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Default implementation of [ApplicationCommandsMessagesFactory].
 *
 * Instances returned by this factory are [DefaultApplicationCommandsMessages],
 * which uses [LocalizationService] to get a localization bundle using the provided [bundleName] first,
 * then [commonBundleName] if a template isn't defined.
 *
 * By default, an instance of this factory is registered using the default bundle name (`ApplicationCommandsMessages`),
 * and `BotCommandsMessages` as the fallback bundle,
 * you can override the default factory by providing a corresponding service factory.
 *
 * ### Light customization / Supporting more locales
 *
 * With the default values, the localization templates would be loaded from `/bc_localization/ApplicationCommandsMessages-default.json`,
 * and `/bc_localization/BotCommandsMessages-default.json`.
 *
 * You may change the values by creating a new `ApplicationCommandsMessages.json`/`BotCommandsMessages.json`.
 *
 * You can also support more locales following by appending an underscore and the [language tag][Locale.toLanguageTag],
 * such as `ApplicationCommandsMessages_fr.json`.
 *
 * The localization paths must be identical to those used by [DefaultApplicationCommandsMessages],
 * but the placeholders can be moved or removed, but not renamed.
 *
 * Refer to [Localization] for mode customization details.
 *
 * @see Localization
 */
class DefaultApplicationCommandsMessagesFactory(
    private val permissionLocalization: PermissionLocalization,
    private val localizationService: LocalizationService,
    private val userLocaleProvider: UserLocaleProvider,
    private val bundleName: String = "ApplicationCommandsMessages",
    private val commonBundleName: String = "BotCommandsMessages",
) : ApplicationCommandsMessagesFactory {

    private val cache: MutableMap<Locale, DefaultApplicationCommandsMessages> = hashMapOf()
    private val lock = ReentrantLock()

    override fun get(locale: Locale): DefaultApplicationCommandsMessages {
        cache[locale]?.let { return it }

        return lock.withLock {
            cache.getOrPut(locale) { DefaultApplicationCommandsMessages(permissionLocalization, localizationService, locale, bundleName, commonBundleName) }
        }
    }

    override fun get(event: Interaction): DefaultApplicationCommandsMessages = get(userLocaleProvider.getLocale(event))
}
