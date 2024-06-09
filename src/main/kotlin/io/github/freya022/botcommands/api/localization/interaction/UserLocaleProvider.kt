package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Provides the locale of a user in a Discord interaction,
 * may be useful if the user has set its own locale, for example.
 *
 * This returns [Interaction.getUserLocale] by default.
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see LocalizableInteraction
 * @see AppLocalizationContext
 */
@InterfacedService(acceptMultiple = false)
interface UserLocaleProvider {
    fun getDiscordLocale(interaction: Interaction): DiscordLocale

    fun getLocale(interaction: Interaction): Locale =
        getDiscordLocale(interaction).toLocale()
}