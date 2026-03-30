package io.github.freya022.botcommands.api.components.exceptions

/**
 * Exception thrown when a component got removed (unregistered, not necessarily from a message)
 * while a coroutine was awaiting a component interaction.
 */
class RemovedComponentException internal constructor(message: String) : ComponentCancellationException(message)
