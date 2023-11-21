package io.github.freya022.botcommands.internal.components.timeout

internal interface HandlerContainer {
    operator fun get(handlerName: String): TimeoutDescriptor<*>?
}
