package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.getDeepestCause
import com.freya02.botcommands.internal.throwUser
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@BService
internal class ModalListener(private val context: BContextImpl, private val modalHandlerContainer: ModalHandlerContainer, private val modalMaps: ModalMaps) {
    private val logger = Logging.getLogger()

    @BEventListener
    suspend fun onModalEvent(event: ModalInteractionEvent) {
        context.config.coroutineScopesConfig.modalsScope.launch {
            try {
                val modalData = modalMaps.consumeModal(event.modalId)
                if (modalData == null) { //Probably the modal expired
                    event.reply_(context.getDefaultMessages(event).modalExpiredErrorMsg, ephemeral = true).queue()
                    return@launch
                }

                val modalHandler: ModalHandlerInfo = modalHandlerContainer[modalData.handlerName]
                    ?: throwUser("Found no modal handler with handler name: '${modalData.handlerName}'")

                modalHandler.execute(context, modalData, event)
            } catch (e: Throwable) {
                context.uncaughtExceptionHandler?.let { handler ->
                    handler.onException(context, event, e)
                    return@launch
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
}