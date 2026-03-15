package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Provides the locale of a guild in a Discord interaction.
 *
 * You may provide a custom implementation, for example, if a guild's locale depends on some external setting.
 *
 * This returns [interaction.getGuildLocale().toLocale()][Interaction.getGuildLocale] by default.
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
    fun getLocale(interaction: Interaction): Locale
}
