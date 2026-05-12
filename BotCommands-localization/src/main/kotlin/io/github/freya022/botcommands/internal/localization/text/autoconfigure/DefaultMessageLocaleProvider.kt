package io.github.freya022.botcommands.internal.localization.text.autoconfigure

import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import net.dv8tion.jda.api.entities.Message
import java.util.Locale

internal object DefaultMessageLocaleProvider : MessageLocaleProvider {

    override fun getLocale(message: Message): Locale = message.guild.locale.toLocale()
}
