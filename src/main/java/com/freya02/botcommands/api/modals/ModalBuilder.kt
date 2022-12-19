package com.freya02.botcommands.api.modals

import com.freya02.botcommands.internal.modals.*
import com.freya02.botcommands.internal.throwInternal
import net.dv8tion.jda.internal.utils.Checks
import java.util.concurrent.TimeUnit

class ModalBuilder internal constructor(
    private val modalMaps: ModalMaps,
    title: String
) : AbstractModalBuilder("0", title) {
    private var handlerData: IModalHandlerData? = null
    private var timeoutInfo: ModalTimeoutInfo? = null

    override fun bindTo(handlerName: String, vararg userData: Any): ModalBuilder = this.also {
        handlerData = PersistentModalHandlerData(handlerName, userData)
    }

    override fun bindTo(handler: EphemeralModalHandler): ModalBuilder = this.also {
        handlerData = EphemeralModalHandlerData(handler)
    }

    override fun setTimeout(timeout: Long, unit: TimeUnit, onTimeout: Runnable): ModalBuilder = this.also {
        Checks.positive(timeout, "Timeout")
        timeoutInfo = ModalTimeoutInfo(timeout, unit, onTimeout)
    }

    override fun setId(customId: String): ModalBuilder = this.also {
        super.setId(customId)
    }

    override fun build(): Modal {
        //Extract input data into this map
        val inputDataMap: Map<String, InputData> = actionRows
            .flatMap { it.actionComponents }
            .filter { it.id != null }
            .associate { actionComponent ->
                val id = actionComponent.id ?: throwInternal("Non identifiable components should have been filtered")

                val data = modalMaps.consumeInput(id)
                    ?: throw IllegalStateException("Modal component with id '$id' could not be found in the inputs created with the '${Modals::class.simpleName}' class")
                id to data
            }

        id = modalMaps.insertModal(PartialModalData(handlerData, inputDataMap, timeoutInfo), id)

        return Modal(super.build(), modalMaps)
    }
}