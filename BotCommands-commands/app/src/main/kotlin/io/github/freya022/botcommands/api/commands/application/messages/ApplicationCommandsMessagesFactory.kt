package io.github.freya022.botcommands.api.commands.application.messages

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Factory of [ApplicationCommandsMessages], the default implementation is [DefaultApplicationCommandsMessagesFactory].
 *
 * ### Complete customization
 *
 * Returning a [ApplicationCommandsMessagesFactory] from a service factory will disable the default implementation,
 * this will let you return a completely custom instance,
 * in which you can craft entirely custom messages in any way you see fit.
 */
@InterfacedService(acceptMultiple = false)
interface ApplicationCommandsMessagesFactory {
    /**
     * Retrieves a [ApplicationCommandsMessages] instance for the given locale.
     */
    fun get(locale: Locale): ApplicationCommandsMessages

    /**
     * Retrieves a [ApplicationCommandsMessages] instance, with the locale derived from this interaction.
     *
     * By default, this uses [UserLocaleProvider] to get the locale.
     */
    fun get(event: Interaction): ApplicationCommandsMessages
}
