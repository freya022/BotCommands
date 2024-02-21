package io.github.freya022.botcommands.internal.modals

import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import io.github.freya022.botcommands.api.modals.Modal
import io.github.freya022.botcommands.api.modals.ModalBuilder
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import kotlin.time.Duration

internal class ModalBuilderImpl internal constructor(
    private val modalMaps: ModalMaps,
    title: String
) : ModalBuilder("0", title) {
    private var handlerData: IModalHandlerData? = null
    private var timeoutInfo: ModalTimeoutInfo? = null

    override fun bindTo(handlerName: String, userData: List<Any?>): ModalBuilderImpl = this.also {
        handlerData = PersistentModalHandlerData(handlerName, userData)
    }

    override fun bindTo(handler: suspend (ModalInteractionEvent) -> Unit): ModalBuilderImpl = this.also {
        handlerData = EphemeralModalHandlerData(handler)
    }

    override fun timeout(timeout: Duration, onTimeout: (suspend () -> Unit)?): ModalBuilder = this.also {
        require(timeout.isFinite() && timeout.isPositive()) {
            "Timeout must be finite and positive"
        }
        timeoutInfo = ModalTimeoutInfo(timeout, onTimeout)
    }

    @Deprecated("Cannot set an ID on modals managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): ModalBuilderImpl = this.also {
        if (customId == "0") return@also // Super constructor call
        throw UnsupportedOperationException("Cannot set an ID on modals managed by the framework")
    }

    override fun build(): Modal {
        //Extract input data into this map
        val inputDataMap: TLongObjectMap<InputData> = TLongObjectHashMap()
        components
            .flatMap { it.actionComponents }
            .filter { it.id != null }
            .forEach { actionComponent ->
                val id = actionComponent.id ?: throwInternal("Non identifiable components should have been filtered")
                val internalId = ModalMaps.parseInputId(id)

                val data = modalMaps.consumeInput(internalId)
                    ?: throw IllegalStateException("Modal component with id '$internalId' could not be found in the inputs created with the '${classRef<Modals>()}' class")
                inputDataMap.put(internalId, data)
            }

        internetSetId(modalMaps.insertModal(PartialModalData(
            handlerData,
            inputDataMap,
            timeoutInfo ?: ModalTimeoutInfo(Modals.defaultTimeout, null)
        )))

        return Modal(jdaBuild(), modalMaps)
    }
}