package io.github.freya022.botcommands.api.commands.text.messages

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Factory of [TextCommandsMessages], the default implementation is [DefaultTextCommandsMessagesFactory].
 *
 * ### Complete customization
 *
 * Returning a [TextCommandsMessagesFactory] from a service factory will disable the default implementation,
 * this will let you return a completely custom instance,
 * in which you can craft entirely custom messages in any way you see fit.
 */
@InterfacedService(acceptMultiple = false)
interface TextCommandsMessagesFactory {
    /**
     * Retrieves a [TextCommandsMessages] instance for the given locale.
     */
    fun get(locale: Locale): TextCommandsMessages

    /**
     * Retrieves a [TextCommandsMessages] instance, with the locale derived from this event.
     *
     * By default, this uses [TextCommandLocaleProvider] to get the locale.
     */
    fun get(event: MessageReceivedEvent): TextCommandsMessages

    /**
     * Retrieves a [TextCommandsMessages] instance, with the locale derived from this interaction.
     *
     * By default, this uses [UserLocaleProvider] to get the locale.
     */
    fun get(event: Interaction): TextCommandsMessages
}
