package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.internal.core.reflection.MemberFunction

internal interface HandlerContainer {
    operator fun get(handlerName: String): MemberFunction<*>?
}
