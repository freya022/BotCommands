package dev.freya02.botcommands.typesafe.messages.api

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi

@ExperimentalTypesafeMessagesApi
enum class LocaleScope {
    PREFER_USER,
    GUILD,
}
