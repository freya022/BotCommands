package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.utils.TimeoutExceptionAccessor
import com.freya02.botcommands.internal.utils.throwUser
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private const val MAX_ID = Long.MAX_VALUE
//Same amount of digits except every digit is 0 but the first one is 1
private val MIN_ID = 10.0.pow(floor(log10(MAX_ID.toDouble()))).toLong()

@BService
internal class ModalMaps(private val config: BConfig) {
    private val modalLock = ReentrantLock()
    private val inputLock = ReentrantLock()

    private val modalMap: MutableMap<String, ModalData> = hashMapOf()

    //Modals input IDs are temporarily stored here while it waits for its ModalBuilder owner to be built, and it's InputData to be associated with it
    private val inputMap: MutableMap<String, InputData> = hashMapOf()

    fun insertModal(partialModalData: PartialModalData, id: String): String {
        modalLock.withLock {
            if (id == "0") { //If not a user supplied ID
                return insertModal(partialModalData, nextModalId())
            }

            val job = partialModalData.timeoutInfo?.let { timeoutInfo ->
                config.coroutineScopesConfig.cooldownScope.launch {
                    delay(timeoutInfo.unit.toMillis(timeoutInfo.timeout))

                    modalLock.withLock {
                        val data = modalMap.remove(id)
                        if (data != null) { //If the timeout was reached without the modal being used
                            timeoutInfo.onTimeout.run()
                            for (continuation in data.continuations) {
                                continuation.cancel(TimeoutExceptionAccessor.createModalTimeoutException())
                            }
                        }
                    }
                }
            }

            modalMap[id] = ModalData(partialModalData, job)
        }

        return id
    }

    fun insertInput(inputData: InputData, id: String): String {
        inputLock.withLock {
            if (id == "0") {
                return insertInput(inputData, nextInputId())
            }

            inputMap.put(id, inputData)
        }

        return id
    }

    fun insertContinuation(id: String, continuation: CancellableContinuation<ModalInteractionEvent>) {
        val data = modalMap[id] ?: throwUser("Unable to find a modal with id '$id' ; Is the modal created with the framework ?")
        data.continuations.add(continuation)
    }

    fun removeContinuation(id: String, continuation: CancellableContinuation<ModalInteractionEvent>) {
        val data = modalMap[id]
        data?.continuations?.remove(continuation)
    }

    fun consumeModal(modalId: String): ModalData? {
        modalLock.withLock {
            return modalMap.remove(modalId)?.also { it.cancelTimeout() }
        }
    }

    fun consumeInput(inputId: String): InputData? {
        inputLock.withLock { return inputMap.remove(inputId) }
    }

    private fun nextModalId(): String {
        val random = ThreadLocalRandom.current()
        modalLock.withLock {
            return generateId(random, modalMap)
        }
    }

    private fun nextInputId(): String {
        val random = ThreadLocalRandom.current()
        inputLock.withLock {
            return generateId(random, inputMap)
        }
    }

    private fun generateId(random: ThreadLocalRandom, map: Map<String, *>): String {
        while (true) {
            val id = random.nextLong(MIN_ID, MAX_ID).toString()

            if (!map.containsKey(id)) {
                return id
            }
        }
    }
}