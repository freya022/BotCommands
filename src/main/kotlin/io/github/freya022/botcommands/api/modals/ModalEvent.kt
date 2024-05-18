package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.modals.ModalMapping

class ModalEvent internal constructor(
    val context: BContext,
    private val event: ModalInteractionEvent,
) : ModalInteractionEvent(event.jda, event.responseNumber, event.interaction) {

    override fun getRawData() = event.rawData

    @JvmName("getValue")
    operator fun get(component: ActionComponent): ModalMapping {
        require(component.isModalCompatible) {
            "Can only get modal mapping for modal-compatible components, provided: $component"
        }

        return event.values.first { it.id == component.id }
            ?: throw IllegalArgumentException("No value found, you likely passed an input from another modal, or haven't attached it")
    }
}