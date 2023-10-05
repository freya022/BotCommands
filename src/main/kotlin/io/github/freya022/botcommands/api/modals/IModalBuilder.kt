package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.internal.modals.ModalDSL
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@ModalDSL
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