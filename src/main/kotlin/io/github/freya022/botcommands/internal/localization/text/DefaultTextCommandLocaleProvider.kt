package io.github.freya022.botcommands.internal.localization.text

import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale

internal object DefaultTextCommandLocaleProvider : TextCommandLocaleProvider {

    override fun getDiscordLocale(event: MessageReceivedEvent): DiscordLocale = event.guild.locale
}
