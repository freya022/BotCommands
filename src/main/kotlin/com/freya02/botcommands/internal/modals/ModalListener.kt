package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExceptionHandler
import com.freya02.botcommands.internal.throwUser
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import kotlin.coroutines.resume

@BService
internal class ModalListener(private val context: BContextImpl, private val modalHandlerContainer: ModalHandlerContainer, private val modalMaps: ModalMaps) {
    private val logger = KotlinLogging.logger { }
    private val exceptionHandler = ExceptionHandler(context, logger)

    @BEventListener
    suspend fun onModalEvent(event: ModalInteractionEvent) {
        context.config.coroutineScopesConfig.modalsScope.launch {
            try {
                val modalData = modalMaps.consumeModal(event.modalId)
                if (modalData == null) { //Probably the modal expired
                    event.reply_(context.getDefaultMessages(event).modalExpiredErrorMsg, ephemeral = true).queue()
                    return@launch
                }

                for (continuation in modalData.continuations) {
                    continuation.resume(event)
                }

                val handlerData = modalData.handlerData ?: return@launch
                when (handlerData) {
                    is EphemeralModalHandlerData -> handlerData.handler(event)
                    is PersistentModalHandlerData -> {
                        val modalHandler: ModalHandlerInfo = modalHandlerContainer[handlerData.handlerName]
                            ?: throwUser("Found no modal handler with handler name: '${handlerData.handlerName}'")

                        modalHandler.execute(context, modalData, event)
                    }
                }
            } catch (e: Throwable) {
                exceptionHandler.handleException(event, e, "modal handler, ID: '${event.modalId}'")

                when {
                    event.isAcknowledged -> event.hook.send(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
                    else -> event.reply_(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
                }
            }
        }
    }
}