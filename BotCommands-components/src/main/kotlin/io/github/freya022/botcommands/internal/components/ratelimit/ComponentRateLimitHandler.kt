package io.github.freya022.botcommands.internal.components.ratelimit

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitingContext
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.internal.components.controller.ComponentsListener
import io.github.freya022.botcommands.internal.components.data.ActionComponentData
import io.github.freya022.botcommands.internal.ratelimit.NullCancellableRateLimit
import io.github.freya022.botcommands.internal.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.internal.ratelimit.handler.AbstractRateLimitHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

private val componentsListenerLogger = KotlinLogging.loggerOf<ComponentsListener>()

@BService
@RequiresComponents
internal class ComponentRateLimitHandler internal constructor(
    private val context: BContext,
    private val botOwners: BotOwners,
    private val rateLimitContainer: RateLimitContainer,
    private val messagesFactory: BotCommandsMessagesFactory,
    config: BConfig,
) : AbstractRateLimitHandler() {
    private val enableOwnerBypass = config.enableOwnerBypass

    internal suspend fun tryRun(component: ActionComponentData, event: GenericComponentInteractionCreateEvent, block: suspend (CancellableRateLimit) -> Boolean) {
        val rateLimitReference = component.rateLimitReference
        if (rateLimitReference == null) {
            val _ = block(NullCancellableRateLimit)
            return
        }

        if (enableOwnerBypass && event.user in botOwners) {
            val _ = block(NullCancellableRateLimit)
            return
        }

        val group = rateLimitReference.group
        val rateLimitInfo = rateLimitContainer[group]
            ?: run {
                componentsListenerLogger.warn { "Could not find a rate limiter named '$group'" }
                event.reply(messagesFactory.get(event).componentExpired(event)).setEphemeral(true).queue()
                return
            }

        val rateLimitingContext = ComponentRateLimitingContext(context, event, rateLimitReference)
        tryRun(rateLimitingContext, rateLimitInfo, block)
    }
}
