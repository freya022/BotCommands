package io.github.freya022.botcommands.api.localization.text

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

/**
 * Provides the locale to be used for localizing text command responses,
 * may be useful if the user has set its own locale, for example.
 *
 * This returns [Guild.getLocale] by default.
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see LocalizableTextCommand
 */
@InterfacedService(acceptMultiple = false)
interface TextCommandLocaleProvider {
    fun getDiscordLocale(event: MessageReceivedEvent): DiscordLocale

    fun getLocale(event: MessageReceivedEvent): Locale =
        getDiscordLocale(event).toLocale()
}