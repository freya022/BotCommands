package com.freya02.botcommands.internal.modals

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class ModalData(
    partialModalData: PartialModalData,
    private val timeoutJob: Job?
) : IPartialModalData by partialModalData {
    fun cancelTimeout() {
        timeoutJob?.cancel("Cancelled by modal usage")
    }
}