package io.github.freya022.botcommands.internal.core.config

internal abstract class AbstractBotCommandsConfiguration {
    protected fun unusable(): Nothing = throw UnsupportedOperationException("Cannot be used")
}
