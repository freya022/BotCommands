package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.ModalEvent
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

internal class ModalData(
    partialModalData: PartialModalData,
    private val timeoutJob: Job?
) : IPartialModalData by partialModalData {
    val continuations: MutableList<CancellableContinuation<ModalEvent>> = arrayListOf()

    fun cancelTimeout() {
        timeoutJob?.cancel("Cancelled by modal usage")
    }
}