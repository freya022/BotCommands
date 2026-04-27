package io.github.freya022.botcommands.api.core.messages

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Factory of [BotCommandsMessages], the default implementation is [DefaultBotCommandsMessagesFactory].
 *
 * ### Complete customization
 *
 * Returning a [BotCommandsMessagesFactory] from a service factory will disable the default implementation,
 * this will let you return a completely custom instance,
 * in which you can craft entirely custom messages in any way you see fit.
 */
@InterfacedService(acceptMultiple = false)
interface BotCommandsMessagesFactory {
    /**
     * Retrieves a [BotCommandsMessages] instance for the given locale.
     */
    fun get(locale: Locale): BotCommandsMessages

    /**
     * Retrieves a [BotCommandsMessages] instance, with the locale derived from this interaction.
     *
     * By default, this uses [UserLocaleProvider] to get the locale.
     */
    fun get(event: Interaction): BotCommandsMessages
}
