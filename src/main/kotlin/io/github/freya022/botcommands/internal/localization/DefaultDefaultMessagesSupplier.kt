package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DefaultMessagesSupplier
import io.github.freya022.botcommands.api.localization.DefaultMessages
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

//@BService //TODO user-defined implementation detection
internal class DefaultDefaultMessagesSupplier(private val context: BContext) : DefaultMessagesSupplier {
    private val localeDefaultMessagesMap: MutableMap<DiscordLocale, DefaultMessages> = EnumMap(DiscordLocale::class.java)

    override fun get(discordLocale: DiscordLocale): DefaultMessages {
        return localeDefaultMessagesMap.computeIfAbsent(discordLocale) {
            DefaultMessages(context, it.toLocale())
        }
    }
}