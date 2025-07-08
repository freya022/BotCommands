package dev.freya02.botcommands.typesafe.messages.integration

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory

interface MyMessageSource : IMessageSource {

    @LocalizedContent("whats.the.fox.doing")
    fun whatsTheFoxDoing(action: String): String
}

@MessageSourceFactory("myBundle")
interface MyMessageSourceFactory : IMessageSourceFactory<MyMessageSource>
