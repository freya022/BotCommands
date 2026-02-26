package io.github.freya022.botcommands.api.components.exceptions

/**
 * Exception thrown when a component timed out while a coroutine was awaiting a component interaction.
 */
class ComponentTimeoutException internal constructor(message: String) : ComponentCancellationException(message)
