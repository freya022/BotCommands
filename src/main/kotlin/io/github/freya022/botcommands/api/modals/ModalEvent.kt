package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.localization.interaction.LocalizableEditCallback
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteractionHook
import io.github.freya022.botcommands.api.localization.interaction.LocalizableReplyCallback
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableEditCallbackImpl
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionImpl
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableReplyCallbackImpl
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.modals.ModalMapping

class ModalEvent internal constructor(
    val context: BContext,
    private val event: ModalInteractionEvent,
    private val localizableInteraction: LocalizableInteractionImpl
) : ModalInteractionEvent(event.jda, event.responseNumber, event.interaction),
    LocalizableInteraction by localizableInteraction,
    LocalizableReplyCallback by LocalizableReplyCallbackImpl(event.interaction, localizableInteraction),
    LocalizableEditCallback by LocalizableEditCallbackImpl(event.interaction, localizableInteraction) {

    override fun getHook(): LocalizableInteractionHook {
        return localizableInteraction.getHook()
    }

    override fun getRawData() = event.rawData

    @JvmName("getValue")
    operator fun get(component: ActionComponent): ModalMapping {
        require(component.isModalCompatible) {
            "Can only get modal mapping for modal-compatible components, provided: $component"
        }

        return event.values.first { it.id == component.id }
            ?: throwArgument("No value found, you likely passed an input from another modal, or haven't attached it")
    }
}