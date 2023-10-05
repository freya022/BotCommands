package io.github.freya022.botcommands.internal.components.repositories

import io.github.freya022.botcommands.internal.core.reflection.MemberFunction

internal interface HandlerContainer {
    operator fun get(handlerName: String): MemberFunction<*>?
}
