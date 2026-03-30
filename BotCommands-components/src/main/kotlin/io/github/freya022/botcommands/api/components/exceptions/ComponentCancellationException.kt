package io.github.freya022.botcommands.api.components.exceptions

import kotlinx.coroutines.CancellationException

/**
 * Exception thrown when a component is canceled (whether timeout or removal) while a coroutine was awaiting a component interaction.
 */
abstract class ComponentCancellationException internal constructor(message: String) : CancellationException(message)
