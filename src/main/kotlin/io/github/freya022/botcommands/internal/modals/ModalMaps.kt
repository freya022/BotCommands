package io.github.freya022.botcommands.internal.modals

import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.utils.TimeoutExceptionAccessor
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.launchCatchingDelayed
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellableContinuation
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val logger = KotlinLogging.logger { }

private const val MODAL_PREFIX = "BotCommands-Modal-"
private const val MODAL_PREFIX_LENGTH = MODAL_PREFIX.length

private const val INPUT_PREFIX = "BotCommands-ModalInput-"
private const val INPUT_PREFIX_LENGTH = INPUT_PREFIX.length

private const val MAX_ID = Long.MAX_VALUE
//Same amount of digits except every digit is 0 but the first one is 1
private val MIN_ID = 10.0.pow(floor(log10(MAX_ID.toDouble()))).toLong()

@BService
internal class ModalMaps(context: BContext) {
    private val timeoutScope = context.coroutineScopesConfig.modalTimeoutScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    private val modalLock = ReentrantLock()
    private val inputLock = ReentrantLock()

    private val modalMap: TLongObjectMap<ModalData> = TLongObjectHashMap()

    //Modals input IDs are temporarily stored here while it waits for its ModalBuilder owner to be built, and it's InputData to be associated with it
    private val inputMap: TLongObjectMap<InputData> = TLongObjectHashMap()

    fun insertModal(partialModalData: PartialModalData): String {
        return modalLock.withLock {
            val internalId: Long = generateId(modalMap)

            val job = partialModalData.timeoutInfo?.let { timeoutInfo ->
                // Run timeout user code on the modal scope again
                timeoutScope.launchCatchingDelayed(timeoutInfo.timeout, { handleTimeoutException(it) }) {
                    val data = modalLock.withLock { modalMap.remove(internalId) }
                    if (data != null) { //If the timeout was reached without the modal being used
                        if (data.continuations.isNotEmpty()) {
                            val timeoutException = TimeoutExceptionAccessor.createModalTimeoutException()
                            for (continuation in data.continuations) {
                                continuation.cancel(timeoutException)
                            }
                        }
                        timeoutInfo.onTimeout?.invoke()
                    }
                }
            }

            modalMap.put(internalId, ModalData(partialModalData, job))
            getModalId(internalId)
        }
    }

    private fun handleTimeoutException(e: Throwable) {
        exceptionHandler.handleException(null, e, "modal timeout handler", emptyMap())
    }

    fun insertInput(inputData: InputData): String {
        return inputLock.withLock {
            val internalId: Long = generateId(inputMap)

            inputMap.put(internalId, inputData)
            getInputId(internalId)
        }
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

    fun consumeInput(inputId: Long): InputData? {
        inputLock.withLock { return inputMap.remove(inputId) }
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

        internal fun isCompatibleInput(id: String): Boolean = id.startsWith(INPUT_PREFIX)
        internal fun parseInputId(id: String): Long {
            require(isCompatibleInput(id)) {
                "Cannot use JDA modal inputs ($id), please use modal inputs from ${classRef<Modals>()}"
            }
            return java.lang.Long.parseLong(id, INPUT_PREFIX_LENGTH, id.length, 10)
        }
        internal fun getInputId(internalId: Long): String = INPUT_PREFIX + internalId
    }
}