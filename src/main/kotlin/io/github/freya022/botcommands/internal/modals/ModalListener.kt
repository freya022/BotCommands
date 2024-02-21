package io.github.freya022.botcommands.internal.modals

import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.launchCatching
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import kotlin.coroutines.resume

private val logger = KotlinLogging.logger { }

@BService
internal class ModalListener(private val context: BContextImpl, private val modalHandlerContainer: ModalHandlerContainer, private val modalMaps: ModalMaps) {
    private val scope = context.coroutineScopesConfig.modalScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    @BEventListener
    suspend fun onModalEvent(event: ModalInteractionEvent) {
        logger.trace { "Received modal interaction '${event.modalId}' with ${event.values.associate { it.id to it.asString }}" }

        scope.launchCatching({ handleException(it, event) }) launch@{
            if (!ModalMaps.isCompatibleModal(event.modalId)) {
                return@launch logger.error { "Received an interaction for an external modal format: '${event.modalId}', " +
                        "please use ${classRef<Modals>()} to make modals" }
            }

            val modalData = modalMaps.consumeModal(ModalMaps.parseModalId(event.modalId))
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
                        ?: throwUser("Missing ${annotationRef<ModalHandler>()} named '${handlerData.handlerName}'")

                    modalHandler.execute(modalData, event)
                }
            }
        }
    }

    private fun handleException(e: Throwable, event: ModalInteractionEvent) {
        exceptionHandler.handleException(event, e, "modal handler, ID: '${event.modalId}'", buildMap(2) {
            event.message?.let { put("Message", it.jumpUrl) }
            put("Modal values", event.values.associate { it.id to it.asString })
        })

        if (event.isAcknowledged) {
            event.hook.send(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
        } else {
            event.reply_(context.getDefaultMessages(event.guild).generalErrorMsg, ephemeral = true).queue()
        }
    }
}