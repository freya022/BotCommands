package io.github.freya022.botcommands.api.localization

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Factory for [DefaultMessages] instances, using locales from various sources.
 *
 * **Usage:** Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see InterfacedService @InterfacedService
 */
interface DefaultMessagesFactory {
    /**
     * Retrieves a [DefaultMessages] instance for the given locale.
     */
    fun get(locale: Locale): DefaultMessages

    /**
     * Retrieves a [DefaultMessages] instance, with the locale derived from this event.
     *
     * By default, this uses [TextCommandLocaleProvider] to get the locale.
     */
    fun get(event: MessageReceivedEvent): DefaultMessages

    /**
     * Retrieves a [DefaultMessages] instance, with the locale derived from this interaction.
     *
     * By default, this uses [UserLocaleProvider] to get the locale.
     */
    fun get(event: Interaction): DefaultMessages
}
