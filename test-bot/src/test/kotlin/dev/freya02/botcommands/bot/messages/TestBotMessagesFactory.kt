package dev.freya02.botcommands.bot.messages

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory

@MessageSourceFactory(bundleName = "Test", ignoreEmptyLocales = true)
@OptIn(ExperimentalTypesafeMessagesApi::class)
interface TestBotMessagesFactory : IMessageSourceFactory<TestBotMessages>
