package io.github.freya022.botcommands.internal.modals

import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.api.modals.exceptions.ModalTimeoutException
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val logger = KotlinLogging.logger { }

private const val MODAL_PREFIX = "BotCommands-Modal-"
private const val MODAL_PREFIX_LENGTH = MODAL_PREFIX.length

private const val MAX_ID = Long.MAX_VALUE
//Same amount of digits except every digit is 0 but the first one is 1
private val MIN_ID = 10.0.pow(floor(log10(MAX_ID.toDouble()))).toLong()

@BService
@RequiresModals
internal class ModalMaps(context: BContext) {
    private val timeoutScope = context.coroutineScopesConfig.modalTimeoutScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    private val modalLock = ReentrantLock()
    private val modalMap: TLongObjectMap<ModalData> = TLongObjectHashMap()

    fun insertModal(partialModalData: PartialModalData): String {
        return modalLock.withLock {
            val internalId: Long = generateId(modalMap)

            val job = partialModalData.timeoutInfo?.let { timeoutInfo ->
                timeoutScope.launch {
                    delay(timeoutInfo.timeout)
                    onTimeout(internalId, timeoutInfo)
                }
            }

            modalMap.put(internalId, ModalData(partialModalData, job))
            getModalId(internalId)
        }
    }

    private suspend fun onTimeout(internalId: Long, timeoutInfo: ModalTimeoutInfo) {
        try {
            val data = modalLock.withLock { modalMap.remove(internalId) }
            if (data != null) { //If the timeout was reached without the modal being used
                if (data.continuations.isNotEmpty()) {
                    val timeoutException = ModalTimeoutException("Timed out waiting for modal")
                    for (continuation in data.continuations) {
                        continuation.cancel(timeoutException)
                    }
                }
                timeoutInfo.onTimeout?.invoke()
            }
        } catch (e: Exception) {
            handleTimeoutException(e)
        }
    }

    private fun handleTimeoutException(e: Throwable) {
        if (e is CancellationException)
            return logger.trace(e) { "Modal timeout handler was cancelled" }

        exceptionHandler.handleException(null, e, "modal timeout handler", emptyMap())
    }

    fun insertContinuation(modalId: Long, continuation: CancellableContinuation<ModalEvent>) {
        val data = modalMap[modalId] ?: throwInternal("Unable to find a modal with id '$modalId'")
        data.continuations.add(continuation)
    }

    fun removeContinuation(modalId: Long, continuation: CancellableContinuation<ModalEvent>) {
        val data = modalMap[modalId]
        data?.continuations?.remove(continuation)
    }

    fun consumeModal(modalId: Long): ModalData? = modalLock.withLock {
       modalMap.remove(modalId)?.also { it.cancelTimeout() }
    }

    private fun generateId(map: TLongObjectMap<*>): Long {
        val random = ThreadLocalRandom.current()
        while (true) {
            val internalId = random.nextLong(MIN_ID, MAX_ID)
            if (!map.containsKey(internalId)) {
                return internalId
            }
        }
    }

    internal companion object {
        internal fun isCompatibleModal(id: String): Boolean = id.startsWith(MODAL_PREFIX)
        internal fun parseModalId(id: String): Long {
            require(isCompatibleModal(id)) {
                "Cannot use JDA modals ($id), please use modals from ${classRef<Modals>()}"
            }
            return java.lang.Long.parseLong(id, MODAL_PREFIX_LENGTH, id.length, 10)
        }
        internal fun getModalId(internalId: Long): String = MODAL_PREFIX + internalId
    }
}
