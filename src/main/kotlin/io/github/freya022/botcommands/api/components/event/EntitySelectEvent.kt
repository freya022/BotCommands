package io.github.freya022.botcommands.api.components.event

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent

class EntitySelectEvent internal constructor(
    val context: BContext,
    event: EntitySelectInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : EntitySelectInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit