package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.getDeepestCause
import com.freya02.botcommands.modals.internal.ModalHandlerContainer
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@BService
internal class ModalListener(private val context: BContextImpl, private val modalHandlerContainer: ModalHandlerContainer) {
    private val logger = Logging.getLogger()

    @BEventListener
    suspend fun onModalEvent(event: ModalInteractionEvent) {
        try {
            val modalData = context.modalMaps.consumeModal(event.modalId)

            if (modalData == null) { //Probably the modal expired
                event.reply_(context.getDefaultMessages(event).modalExpiredErrorMsg, ephemeral = true).queue()
                return
            }

            val modalHandler: ModalHandlerInfo = modalHandlerContainer[modalData.handlerName] ?: let {
                //TODO user error message
                logger.error("Got no modal handler for handler name: '${modalData.handlerName}'")
                return
            }

            modalHandler.execute(context, modalData, event)
        } catch (e: Throwable) {
            context.uncaughtExceptionHandler?.let { handler ->
                handler.onException(context, event, e)
                return
            }

            val baseEx = e.getDeepestCause()

            logger.error("Unhandled exception while executing a modal handler", baseEx)
            when {
                event.isAcknowledged -> event.hook.send(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
                else -> event.reply_(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
            }

            context.dispatchException("Exception in modal handler", baseEx)
        }
    }
}