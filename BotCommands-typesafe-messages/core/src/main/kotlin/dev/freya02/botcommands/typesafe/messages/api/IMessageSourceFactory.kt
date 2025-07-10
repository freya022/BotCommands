package dev.freya02.botcommands.typesafe.messages.api

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import net.dv8tion.jda.api.interactions.Interaction

@ExperimentalTypesafeMessagesApi
interface IMessageSourceFactory<out T : IMessageSource> {
    fun create(interaction: Interaction): T
}
