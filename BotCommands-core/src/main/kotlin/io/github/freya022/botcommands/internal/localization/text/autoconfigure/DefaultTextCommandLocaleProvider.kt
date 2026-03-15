package io.github.freya022.botcommands.internal.localization.text.autoconfigure

import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.*

internal object DefaultTextCommandLocaleProvider : TextCommandLocaleProvider {

    override fun getLocale(event: MessageReceivedEvent): Locale = event.guild.locale.toLocale()
}
