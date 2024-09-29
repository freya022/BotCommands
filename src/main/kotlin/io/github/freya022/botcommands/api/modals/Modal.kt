package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.internal.modals.ModalMaps
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.interactions.modals.Modal as JDAModal

class Modal internal constructor(modal: JDAModal, private val modalMaps: ModalMaps) : JDAModal by modal {
    /**
     * Suspends until the modal is submitted, and returns the event.
     *
     * @throws TimeoutCancellationException If the timeout set in the modal builder has been reached
     */
    @JvmSynthetic
    suspend fun await(): ModalEvent {
        return suspendCancellableCoroutine { continuation ->
            val internalId = ModalMaps.parseModalId(id)
            modalMaps.insertContinuation(internalId, continuation)

            continuation.invokeOnCancellation {
                modalMaps.removeContinuation(internalId, continuation)
            }
        }
    }

    /**
     * Suspends until the modal is submitted, and returns the event,
     * or `null` if the timeout has been reached.
     */
    @JvmSynthetic
    suspend fun awaitOrNull(): ModalEvent? = try {
        await()
    } catch (e: TimeoutCancellationException) {
        null
    }
}