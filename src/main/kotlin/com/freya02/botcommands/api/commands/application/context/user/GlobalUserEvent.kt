package com.freya02.botcommands.api.commands.application.context.user

import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import com.freya02.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

open class GlobalUserEvent internal constructor(
    val context: BContext,
    event: UserContextInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : UserContextInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit