package com.freya02.botcommands.api.components.event

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent

class EntitySelectEvent internal constructor(
    val context: BContext,
    event: EntitySelectInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : EntitySelectInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit