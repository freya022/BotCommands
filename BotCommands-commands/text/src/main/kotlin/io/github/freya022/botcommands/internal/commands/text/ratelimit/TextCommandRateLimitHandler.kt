package io.github.freya022.botcommands.internal.commands.text.ratelimit

import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.ratelimit.NullCancellableRateLimit
import io.github.freya022.botcommands.internal.ratelimit.handler.AbstractRateLimitHandler
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
@RequiresTextCommands
internal class TextCommandRateLimitHandler internal constructor(
    private val context: BContext,
    private val botOwners: BotOwners,
    config: BConfig,
) : AbstractRateLimitHandler() {
    private val enableOwnerBypass = config.enableOwnerBypass

    internal suspend fun tryRun(commandInfo: TextCommandInfoImpl, event: MessageReceivedEvent, block: suspend (CancellableRateLimit) -> Boolean) {
        val rateLimitInfo = commandInfo.rateLimitInfo
        if (rateLimitInfo == null) {
            val _ = block(NullCancellableRateLimit)
            return
        }

        if (enableOwnerBypass && event.author in botOwners) {
            val _ = block(NullCancellableRateLimit)
            return
        }

        val rateLimitingContext = TextCommandRateLimitingContext(context, event, commandInfo)
        tryRun(rateLimitingContext, rateLimitInfo, block)
    }
}
