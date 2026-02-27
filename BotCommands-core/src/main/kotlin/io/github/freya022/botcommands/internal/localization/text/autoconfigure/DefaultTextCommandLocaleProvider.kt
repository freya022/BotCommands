package io.github.freya022.botcommands.internal.localization.text.autoconfigure

import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

internal object DefaultTextCommandLocaleProvider : TextCommandLocaleProvider {

    @Deprecated("Use 'getLocale' instead, use 'DiscordLocale.from' / 'DiscordLocale#toLocale' if necessary")
    override fun getDiscordLocale(event: MessageReceivedEvent): DiscordLocale = event.guild.locale

    override fun getLocale(event: MessageReceivedEvent): Locale = event.guild.locale.toLocale()
}
