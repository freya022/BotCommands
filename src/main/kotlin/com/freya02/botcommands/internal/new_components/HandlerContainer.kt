package com.freya02.botcommands.internal.new_components

import kotlin.reflect.KFunction

interface HandlerContainer {
    operator fun get(handlerName: String): KFunction<*>?
}