package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BModalsConfig
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionFactory
import io.github.freya022.botcommands.internal.modals.utils.allValuesAsString
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import kotlin.coroutines.resume

private val logger = KotlinLogging.logger { }

@BService
@RequiresModals
internal class ModalListener(
    private val context: BContextImpl,
    private val messagesFactory: BotCommandsMessagesFactory,
    private val localizableInteractionFactory: LocalizableInteractionFactory,
    private val modalHandlerContainer: ModalHandlerContainer,
    private val modalMaps: ModalMaps,
) {

    private val scope = context.coroutineScopesConfig.modalScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    @BEventListener
    suspend fun onModalEvent(jdaEvent: ModalInteractionEvent) {
        logger.trace { "Received modal interaction '${jdaEvent.modalId}' with ${jdaEvent.allValuesAsString}" }

        scope.launchCatching({ handleException(it, jdaEvent) }) launch@{
            if (!ModalMaps.isCompatibleModal(jdaEvent.modalId)) {
                return@launch logger.error { "Received an interaction for an external modal format: '${jdaEvent.modalId}', " +
                        "please use ${classRef<Modals>()} to make modals or disable them with ${BModalsConfig::enable.reference}" }
            }

            val modalData = modalMaps.consumeModal(ModalMaps.parseModalId(jdaEvent.modalId))
            if (modalData == null) { //Probably the modal expired
                jdaEvent.reply(messagesFactory.get(jdaEvent).modalExpired(jdaEvent)).setEphemeral(true).queue()
                return@launch
            }

            val localizableInteraction = localizableInteractionFactory.create(jdaEvent)
            val event = ModalEvent(context, jdaEvent, localizableInteraction)
            for (continuation in modalData.continuations) {
                continuation.resume(event)
            }

            val handlerData = modalData.handlerData ?: return@launch
            when (handlerData) {
                is EphemeralModalHandlerData -> handlerData.handler(event)
                is PersistentModalHandlerData -> {
                    val modalHandler: ModalHandlerInfo = modalHandlerContainer[handlerData.handlerName]
                        ?: throwArgument("Missing ${annotationRef<ModalHandler>()} named '${handlerData.handlerName}'")

                    modalHandler.execute(modalData, event)
                }
            }
        }
    }

    private suspend fun handleException(e: Throwable, event: ModalInteractionEvent) {
        exceptionHandler.handleException(event, e, "modal handler, ID: '${event.modalId}'", buildMap(2) {
            event.message?.let { put("Message", it.jumpUrl) }
            put("Modal values", event.allValuesAsString)
        })
        if (e is InsufficientPermissionException) {
            event.replyExceptionMessage(messagesFactory.get(event).missingBotPermissions(event, setOf(e.permission)))
        } else {
            event.replyExceptionMessage(messagesFactory.get(event).uncaughtException(event))
        }
    }
}
