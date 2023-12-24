package io.github.freya022.botcommands.internal.modals

import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import kotlin.coroutines.resume

@BService
internal class ModalListener(private val context: BContextImpl, private val modalHandlerContainer: ModalHandlerContainer, private val modalMaps: ModalMaps) {
    private val logger = KotlinLogging.logger { }
    private val exceptionHandler = ExceptionHandler(context, logger)

    @BEventListener
    suspend fun onModalEvent(event: ModalInteractionEvent) {
        context.coroutineScopesConfig.modalsScope.launch {
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

                        modalHandler.execute(modalData, event)
                    }
                }
            } catch (e: Throwable) {
                exceptionHandler.handleException(event, e, "modal handler, ID: '${event.modalId}'", buildMap(2) {
                    event.message?.let { put("Message", it.jumpUrl) }
                    put("Modal values", event.values.associate { it.id to it.asString })
                })

                when {
                    event.isAcknowledged -> event.hook.send(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
                    else -> event.reply_(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
                }
            }
        }
    }
}