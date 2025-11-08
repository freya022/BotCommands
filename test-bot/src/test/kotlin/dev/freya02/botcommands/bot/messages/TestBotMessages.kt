package dev.freya02.botcommands.bot.messages

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

@OptIn(ExperimentalTypesafeMessagesApi::class)
interface TestBotMessages : IMessageSource {

    @LocalizedContent("commands.localization.response")
    fun response(guildUsers: Int, uptime: Double): String
}
