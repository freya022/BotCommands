package com.freya02.botcommands.internal.modals

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

class ModalData(
    partialModalData: PartialModalData,
    private val timeoutJob: Job?
) : IPartialModalData by partialModalData {
    val continuations: MutableList<CancellableContinuation<ModalInteractionEvent>> = arrayListOf()

    fun cancelTimeout() {
        timeoutJob?.cancel("Cancelled by modal usage")
    }
}