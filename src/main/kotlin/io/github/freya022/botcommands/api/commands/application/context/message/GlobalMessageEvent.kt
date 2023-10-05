package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

open class GlobalMessageEvent internal constructor(
    val context: BContext,
    event: MessageContextInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : MessageContextInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit