package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.Modal
import io.github.freya022.botcommands.api.modals.ModalBuilder
import io.github.freya022.botcommands.api.modals.ModalTimeoutInfo
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

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

    override fun bindTo(handler: Consumer<ModalInteractionEvent>): ModalBuilderImpl = this.also {
        return bindTo { handler.accept(it) }
    }

    override fun setTimeout(timeout: Duration, onTimeout: Runnable): ModalBuilder = this.also {
        require(!timeout.isZero && !timeout.isNegative) {
            "Timeout must be positive"
        }
        timeoutInfo = ModalTimeoutInfo(timeout.toMillis(), TimeUnit.MILLISECONDS, onTimeout)
    }

    override fun setId(customId: String): ModalBuilderImpl = this.also {
        super.setId(customId)
    }

    override fun build(): Modal {
        //Extract input data into this map
        val inputDataMap: Map<String, InputData> = components
            .flatMap { it.actionComponents }
            .filter { it.id != null }
            .associate { actionComponent ->
                val id = actionComponent.id ?: throwInternal("Non identifiable components should have been filtered")

                val data = modalMaps.consumeInput(id)
                    ?: throw IllegalStateException("Modal component with id '$id' could not be found in the inputs created with the '${classRef<Modals>()}' class")
                id to data
            }

        id = modalMaps.insertModal(PartialModalData(handlerData, inputDataMap, timeoutInfo), id)

        return Modal(jdaBuild(), modalMaps)
    }
}