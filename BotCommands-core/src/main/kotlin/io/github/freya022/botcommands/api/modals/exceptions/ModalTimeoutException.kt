package io.github.freya022.botcommands.api.modals.exceptions

import kotlinx.coroutines.CancellationException

/**
 * Exception thrown when modal timed out while a coroutine was awaiting a modal interaction
 */
class ModalTimeoutException(message: String) : CancellationException(message)
