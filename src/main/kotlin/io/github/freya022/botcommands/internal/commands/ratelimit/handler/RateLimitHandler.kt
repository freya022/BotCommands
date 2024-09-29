package io.github.freya022.botcommands.internal.commands.ratelimit.handler

import dev.minn.jda.ktx.messages.reply_
import io.github.bucket4j.Bucket
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.ratelimit.CancellableRateLimitImpl
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
    private val defaultMessagesFactory: DefaultMessagesFactory,
    config: BConfig,
) {
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

        val bucket = rateLimitInfo.limiter.getBucket(context, event, commandInfo)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.limiter.onRateLimit(context, event, commandInfo, probe)
        }
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

        val bucket = rateLimitInfo.limiter.getBucket(context, event, commandInfo)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.limiter.onRateLimit(context, event, commandInfo, probe)
        }
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
                val defaultMessages = defaultMessagesFactory.get(event)
                event.reply_(defaultMessages.componentExpiredErrorMsg, ephemeral = true).queue()
                return
            }

        val bucket = rateLimitInfo.limiter.getBucket(context, event, rateLimitReference)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.limiter.onRateLimit(context, event, probe)
        }
    }

    private suspend fun runRateLimited(block: suspend (CancellableRateLimit) -> Boolean, bucket: Bucket) {
        val cancellableRateLimit = CancellableRateLimitImpl(bucket)
        try {
            if (!block(cancellableRateLimit)) {
                cancellableRateLimit.cancelRateLimit()
            }
        } catch (e: Throwable) {
            cancellableRateLimit.cancelRateLimit()
            throw e
        }
    }
}