package com.freya02.botcommands.api.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

interface IModalBuilder {
    /**
     * Binds the following handler to this modal
     *
     * This step is optional if you do not wish to use handlers for that
     *
     * @param handler The modal handler to run when the modal is used
     *
     * @return This builder for chaining convenience
     */
    @JvmSynthetic
    fun bindTo(handler: suspend (ModalInteractionEvent) -> Unit): ModalBuilder
}