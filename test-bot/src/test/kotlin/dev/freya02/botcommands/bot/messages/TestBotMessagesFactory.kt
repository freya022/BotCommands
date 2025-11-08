package dev.freya02.botcommands.bot.messages

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory

@MessageSourceFactory("Test")
@OptIn(ExperimentalTypesafeMessagesApi::class)
interface TestBotMessagesFactory : IMessageSourceFactory<TestBotMessages>
