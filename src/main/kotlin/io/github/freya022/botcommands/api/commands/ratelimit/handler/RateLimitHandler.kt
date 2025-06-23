package io.github.freya022.botcommands.api.commands.ratelimit.handler

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Defines the behavior when a rate limit is triggered.
 */
interface RateLimitHandler {
    suspend fun onRateLimit(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo, probe: ConsumptionProbe)

    suspend fun onRateLimit(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo, probe: ConsumptionProbe)

    suspend fun onRateLimit(context: BContext, event: GenericComponentInteractionCreateEvent, probe: ConsumptionProbe)
}