package com.freya02.botcommands.internal

import com.freya02.botcommands.api.DefaultMessages
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*
import java.util.function.Function

class DefaultMessagesFunction : Function<DiscordLocale, DefaultMessages> {
    private val localeDefaultMessagesMap: MutableMap<DiscordLocale, DefaultMessages> = EnumMap(DiscordLocale::class.java)

    override fun apply(locale: DiscordLocale): DefaultMessages {
        return localeDefaultMessagesMap.computeIfAbsent(locale) { DefaultMessages(Locale.forLanguageTag(it.locale)) }
    }
}