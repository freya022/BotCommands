package com.freya02.botcommands.modals.internal

import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@BService
internal class ModalListener(private val modalHandlerContainer: ModalHandlerContainer) {
    @BEventListener
    fun onModalInteraction(event: ModalInteractionEvent) {
        modalHandlerContainer.handlers
    }
}