package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.Modal
import io.github.freya022.botcommands.api.modals.ModalBuilder
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import kotlin.time.Duration

internal class ModalBuilderImpl internal constructor(
    override val modals: Modals,
    private val modalMaps: ModalMaps,
    title: String
) : ModalBuilder("0", title) {
    private var handlerData: IModalHandlerData? = null
    private var timeoutInfo: ModalTimeoutInfo? = null

    override fun bindTo(handlerName: String, userData: List<Any?>): ModalBuilderImpl = this.also {
        handlerData = PersistentModalHandlerData(handlerName, userData)
    }

    override fun bindTo(handler: suspend (ModalEvent) -> Unit): ModalBuilderImpl = this.also {
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
        internetSetId(modalMaps.insertModal(PartialModalData(
            handlerData,
            timeoutInfo ?: Modals.defaultTimeout.takeIfFinite()?.let { ModalTimeoutInfo(it, null) }
        )))

        return Modal(jdaBuild(), modalMaps)
    }
}
