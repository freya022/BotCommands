package io.github.freya022.botcommands.api.components.event

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonEvent internal constructor(
    val context: BContext,
    event: ButtonInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : ButtonInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit