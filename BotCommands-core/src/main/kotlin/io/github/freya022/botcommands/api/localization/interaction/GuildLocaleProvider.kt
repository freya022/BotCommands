package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Provides the locale of a guild in a Discord interaction,
 * may be useful if the guild has set its own locale, for example.
 *
 * It is recommended to override both [getDiscordLocale] and [getLocale] for best results,
 * when using localization in events, and in [AppLocalizationContext].
 *
 * This returns [Interaction.getGuildLocale] by default.
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService].
 *
 * @see LocalizableInteraction
 * @see AppLocalizationContext
 * @see TextLocalizationContext
 */
@InterfacedService(acceptMultiple = false)
interface GuildLocaleProvider {
    fun getDiscordLocale(interaction: Interaction): DiscordLocale

    fun getLocale(interaction: Interaction): Locale =
        getDiscordLocale(interaction).toLocale()
}