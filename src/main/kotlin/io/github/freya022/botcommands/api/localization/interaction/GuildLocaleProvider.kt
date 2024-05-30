package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Provides the locale of a guild in a Discord interaction,
 * may be useful if the guild has set its own locale, for example.
 *
 * This returns [Interaction.getGuildLocale] by default.
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 */
@InterfacedService(acceptMultiple = false)
fun interface GuildLocaleProvider {
    fun getLocale(interaction: Interaction): Locale
}