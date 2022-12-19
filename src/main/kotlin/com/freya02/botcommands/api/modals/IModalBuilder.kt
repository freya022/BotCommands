package com.freya02.botcommands.api.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

interface IModalBuilder {
    @JvmSynthetic
    fun bindTo(handler: suspend (ModalInteractionEvent) -> Unit): ModalBuilder
}