package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import com.freya02.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal open class GlobalSlashEventImpl(
    context: BContext,
    event: SlashCommandInteractionEvent,
    cancellableRateLimit: CancellableRateLimit
) : GlobalSlashEvent(context, event, cancellableRateLimit)