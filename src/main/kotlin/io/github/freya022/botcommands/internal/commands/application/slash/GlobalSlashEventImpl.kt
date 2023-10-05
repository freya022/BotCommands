package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal open class GlobalSlashEventImpl(
    context: BContext,
    event: SlashCommandInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : GlobalSlashEvent(context, event, cancellableRateLimit)