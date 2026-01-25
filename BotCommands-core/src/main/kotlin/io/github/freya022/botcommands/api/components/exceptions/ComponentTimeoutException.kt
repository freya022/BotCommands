package io.github.freya022.botcommands.api.components.exceptions

import kotlinx.coroutines.CancellationException

/**
 * Exception thrown when a component timed out while a coroutine was awaiting a component interaction.
 */
class ComponentTimeoutException(message: String) : CancellationException(message)
