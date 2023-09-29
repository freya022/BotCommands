package com.freya02.botcommands.api.components.event

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class StringSelectEvent internal constructor(
    val context: BContext,
    event: StringSelectInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : StringSelectInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit