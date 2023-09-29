package com.freya02.botcommands.api.commands.application.context.message

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

open class GlobalMessageEvent internal constructor(
    val context: BContext,
    event: MessageContextInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : MessageContextInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit
