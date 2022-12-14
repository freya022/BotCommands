package com.freya02.botcommands.internal.components.repositories

import kotlin.reflect.KFunction

interface HandlerContainer {
    operator fun get(handlerName: String): KFunction<*>?
}
