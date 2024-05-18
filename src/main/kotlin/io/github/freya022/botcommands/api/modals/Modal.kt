package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.internal.modals.ModalMaps
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.interactions.modals.Modal as JDAModal

class Modal internal constructor(modal: JDAModal, private val modalMaps: ModalMaps) : JDAModal by modal {
    suspend fun await(): ModalEvent {
        return suspendCancellableCoroutine { continuation ->
            val internalId = ModalMaps.parseModalId(id)
            modalMaps.insertContinuation(internalId, continuation)

            continuation.invokeOnCancellation {
                modalMaps.removeContinuation(internalId, continuation)
            }
        }
    }
}