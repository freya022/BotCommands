package com.freya02.botcommands.api.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

fun interface EphemeralModalHandler {
    suspend operator fun invoke(event: ModalInteractionEvent)
}