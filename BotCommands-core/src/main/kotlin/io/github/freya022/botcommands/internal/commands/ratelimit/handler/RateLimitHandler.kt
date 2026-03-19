package io.github.freya022.botcommands.internal.commands.ratelimit.handler

import io.github.freya022.botcommands.api.commands.ratelimit.ApplicationCommandRateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.ratelimit.ComponentRateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.TextCommandRateLimitingContext
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.ratelimit.NullCancellableRateLimit
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentsListener
import io.github.freya022.botcommands.internal.components.data.ActionComponentData
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

private val componentsListenerLogger = KotlinLogging.loggerOf<ComponentsListener>()

@BService
internal class RateLimitHandler internal constructor(
    private val context: BContext,
    private val botOwners: BotOwners,
    private val rateLimitContainer: RateLimitContainer,
    private val messagesFactory: BotCommandsMessagesFactory,
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

    internal suspend fun tryRun(component: ActionComponentData, event: GenericComponentInteractionCreateEvent, block: suspend (CancellableRateLimit) -> Boolean) {
        val rateLimitReference = component.rateLimitReference
        if (rateLimitReference == null) {
            block(NullCancellableRateLimit)
            return
        }

        if (enableOwnerBypass && event.user in botOwners) {
            block(NullCancellableRateLimit)
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
