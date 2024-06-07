package io.github.freya022.botcommands.api.components.event

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.localization.interaction.LocalizableEditCallback
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteractionHook
import io.github.freya022.botcommands.api.localization.interaction.LocalizableReplyCallback
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableEditCallbackImpl
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionImpl
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableReplyCallbackImpl
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class StringSelectEvent internal constructor(
    val context: BContext,
    private val event: StringSelectInteractionEvent,
    cancellableRateLimit: CancellableRateLimit,
    private val localizableInteraction: LocalizableInteractionImpl
) : StringSelectInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit,
    LocalizableInteraction by localizableInteraction,
    LocalizableReplyCallback by LocalizableReplyCallbackImpl(event.interaction, localizableInteraction),
    LocalizableEditCallback by LocalizableEditCallbackImpl(event.interaction, localizableInteraction) {

    override fun getHook(): LocalizableInteractionHook {
        return localizableInteraction.getHook()
    }

    override fun getRawData() = event.rawData
}