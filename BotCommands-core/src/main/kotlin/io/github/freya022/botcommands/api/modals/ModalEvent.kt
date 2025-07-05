package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.*
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableEditCallbackImpl
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionImpl
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableReplyCallbackImpl
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import java.util.*

/**
 * Interaction event for modals.
 *
 * ### Localization
 * You can send localized replies and edit messages using the user, guild and also any [Locale],
 * by using this event directly, but also the interaction hook,
 * see [LocalizableInteraction] for more details and configuration.
 *
 * An alternative to using this event is injecting an [AppLocalizationContext] in a parameter,
 * or retrieving one using [getLocalizationContext].
 *
 * In both cases, you can configure the user and guild locales,
 * using [UserLocaleProvider] and [GuildLocaleProvider].
 */
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