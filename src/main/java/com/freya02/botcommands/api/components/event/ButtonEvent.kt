package com.freya02.botcommands.api.components.event

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonEvent internal constructor(
    val context: BContext,
    event: ButtonInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : ButtonInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit
