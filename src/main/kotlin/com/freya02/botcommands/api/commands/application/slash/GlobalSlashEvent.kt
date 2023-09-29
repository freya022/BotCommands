package com.freya02.botcommands.api.commands.application.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

abstract class GlobalSlashEvent internal constructor(
    val context: BContext,
    event: SlashCommandInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : SlashCommandInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit
