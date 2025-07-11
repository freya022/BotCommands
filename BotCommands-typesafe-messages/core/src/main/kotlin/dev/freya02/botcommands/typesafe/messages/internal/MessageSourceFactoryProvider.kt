package dev.freya02.botcommands.typesafe.messages.internal

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import io.github.freya022.botcommands.api.core.BContext

fun interface MessageSourceFactoryProvider<T : IMessageSourceFactory<*>> {
    fun get(context: BContext): T
}
