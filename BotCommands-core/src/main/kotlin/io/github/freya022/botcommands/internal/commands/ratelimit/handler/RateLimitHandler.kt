package io.github.freya022.botcommands.internal.commands.ratelimit.handler

import io.github.freya022.botcommands.api.commands.ratelimit.ApplicationCommandRateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.ratelimit.TextCommandRateLimitingContext
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.ratelimit.NullCancellableRateLimit
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
internal class RateLimitHandler internal constructor(
    private val context: BContext,
    private val botOwners: BotOwners,
    config: BConfig,
) : AbstractRateLimitHandler() {
    private val enableOwnerBypass = config.enableOwnerBypass

    internal suspend fun tryRun(commandInfo: TextCommandInfoImpl, event: MessageReceivedEvent, block: suspend (CancellableRateLimit) -> Boolean) {
        val rateLimitInfo = commandInfo.rateLimitInfo
        if (rateLimitInfo == null) {
            block(NullCancellableRateLimit)
            return
        }

        if (enableOwnerBypass && event.author in botOwners) {
            block(NullCancellableRateLimit)
            return
        }

        val rateLimitingContext = TextCommandRateLimitingContext(context, event, commandInfo)
        tryRun(rateLimitingContext, rateLimitInfo, block)
    }

    internal suspend fun tryRun(commandInfo: ApplicationCommandInfoImpl, event: GenericCommandInteractionEvent, block: suspend (CancellableRateLimit) -> Boolean) {
        val rateLimitInfo = commandInfo.rateLimitInfo
        if (rateLimitInfo == null) {
            block(NullCancellableRateLimit)
            return
        }

        if (enableOwnerBypass && event.user in botOwners) {
            block(NullCancellableRateLimit)
            return
        }

        val rateLimitingContext = ApplicationCommandRateLimitingContext(context, event, commandInfo)
        tryRun(rateLimitingContext, rateLimitInfo, block)
    }
}
