package io.github.freya022.botcommands.api.commands.ratelimit.handler

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

/**
 * Defines the behavior when a rate limit is triggered.
 */
interface RateLimitHandler {
    suspend fun onRateLimit(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo, probe: ConsumptionProbe)

    suspend fun <T> onRateLimit(context: BContext, event: T, commandInfo: ApplicationCommandInfo, probe: ConsumptionProbe)
            where T : GenericCommandInteractionEvent,
                  T : IReplyCallback

    suspend fun <T> onRateLimit(context: BContext, event: T, probe: ConsumptionProbe)
            where T : GenericComponentInteractionCreateEvent,
                  T : IReplyCallback,
                  T : IMessageEditCallback
}