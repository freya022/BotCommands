package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * Provides the locale of a user in a Discord interaction.
 *
 * You may provide a custom implementation, for example, if a user's locale depends on some external setting.
 *
 * This returns [interaction.getUserLocale().toLocale()][Interaction.getUserLocale] by default.
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService].
 *
 * @see LocalizableInteraction
 * @see AppLocalizationContext
 */
@InterfacedService(acceptMultiple = false)
interface UserLocaleProvider {
    fun getLocale(interaction: Interaction): Locale
}
