package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.internal.core.reflection.MemberFunction

internal interface HandlerContainer {
    operator fun get(handlerName: String): MemberFunction<*>?
}
