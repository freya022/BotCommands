package io.github.freya022.botcommands.internal.commands.application.ratelimit

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfoImpl
import io.github.freya022.botcommands.internal.ratelimit.NullCancellableRateLimit
import io.github.freya022.botcommands.internal.ratelimit.handler.AbstractRateLimitHandler
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

@BService
@RequiresApplicationCommands
internal class ApplicationCommandRateLimitHandler internal constructor(
    private val context: BContext,
    private val botOwners: BotOwners,
    config: BConfig,
) : AbstractRateLimitHandler() {
    private val enableOwnerBypass = config.enableOwnerBypass

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
