package com.freya02.botcommands.api.modals

import com.freya02.botcommands.internal.modals.ModalMaps
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.modals.Modal as JDAModal

class Modal internal constructor(modal: JDAModal, private val modalMaps: ModalMaps) : JDAModal by modal {
    suspend fun await(): ModalInteractionEvent {
        return suspendCancellableCoroutine { continuation ->
            modalMaps.insertContinuation(id, continuation)

            continuation.invokeOnCancellation {
                modalMaps.removeContinuation(id, continuation)
            }
        }
    }
}