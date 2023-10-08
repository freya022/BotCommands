package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

open class GlobalSlashEvent internal constructor(
    val context: BContext,
    event: SlashCommandInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : SlashCommandInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit
