package io.github.freya022.botcommands.internal.components.timeout

internal interface TimeoutHandlerContainer {
    operator fun get(handlerName: String): TimeoutDescriptor<*>?
}
