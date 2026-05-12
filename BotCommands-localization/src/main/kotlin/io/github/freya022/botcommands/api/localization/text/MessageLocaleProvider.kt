package io.github.freya022.botcommands.api.localization.text

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import java.util.Locale

/**
 * Provides the locale to be used for localizing text command responses.
 *
 * You may provide a custom implementation, for example, if a guild/user's locale depends on some external setting.
 *
 * This returns [guild.getLocale().toLocale()][Guild.getLocale] by default.
 *
 * ### Usage
 * Register your instance as a service with [@BService][BService].
 */
@InterfacedService(acceptMultiple = false)
interface MessageLocaleProvider {
    fun getLocale(message: Message): Locale
}
