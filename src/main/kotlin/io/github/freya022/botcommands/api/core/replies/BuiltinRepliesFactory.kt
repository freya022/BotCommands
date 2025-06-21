@file:Suppress("DEPRECATION")

package io.github.freya022.botcommands.api.core.replies

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Factory of [BuiltinReplies], the default implementation is [DefaultBuiltinRepliesFactory], or,
 * if a non-default [DefaultMessagesFactory] exists, an adapter is used.
 *
 * ### Complete customization
 *
 * Returning a [BuiltinRepliesFactory] from a service factory will disable the default implementation,
 * this will let you return a completely custom instance,
 * in which you can craft entirely custom messages in any way you see fit.
 */
@InterfacedService(acceptMultiple = false)
interface BuiltinRepliesFactory {
    /**
     * Retrieves a [BuiltinReplies] instance for the given locale.
     */
    fun get(locale: Locale): BuiltinReplies

    /**
     * Retrieves a [BuiltinReplies] instance, with the locale derived from this event.
     *
     * By default, this uses [TextCommandLocaleProvider] to get the locale.
     */
    fun get(event: MessageReceivedEvent): BuiltinReplies

    /**
     * Retrieves a [BuiltinReplies] instance, with the locale derived from this interaction.
     *
     * By default, this uses [UserLocaleProvider] to get the locale.
     */
    fun get(event: Interaction): BuiltinReplies
}