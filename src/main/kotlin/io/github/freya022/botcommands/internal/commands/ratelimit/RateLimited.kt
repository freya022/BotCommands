package io.github.freya022.botcommands.internal.commands.ratelimit

import dev.minn.jda.ktx.messages.reply_
import io.github.bucket4j.Bucket
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentsListener
import io.github.freya022.botcommands.internal.components.data.ActionComponentData
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

internal interface RateLimited {
    val path: CommandPath
    val rateLimitInfo: RateLimitInfo?
}

internal suspend fun TextCommandInfoImpl.withRateLimit(context: BContext, event: MessageReceivedEvent, isNotOwner: Boolean, block: suspend (CancellableRateLimit) -> Boolean) {
    val rateLimitInfo = rateLimitInfo
    if (isNotOwner && rateLimitInfo != null) {
        val bucket = rateLimitInfo.limiter.getBucket(context, event, this)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.limiter.onRateLimit(context, event, this, probe)
        }
    } else {
        block(NullCancellableRateLimit)
    }
}

internal suspend fun ApplicationCommandInfoImpl.withRateLimit(context: BContext, event: GenericCommandInteractionEvent, isNotOwner: Boolean, block: suspend (CancellableRateLimit) -> Boolean) {
    val rateLimitInfo = rateLimitInfo
    if (isNotOwner && rateLimitInfo != null) {
        val bucket = rateLimitInfo.limiter.getBucket(context, event, this)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.limiter.onRateLimit(context, event, this, probe)
        }
    } else {
        block(NullCancellableRateLimit)
    }
}

internal suspend fun ActionComponentData.withRateLimit(context: BContext, event: GenericComponentInteractionCreateEvent, isOwner: Boolean, block: suspend (CancellableRateLimit) -> Boolean) {
    if (isOwner) {
        block(NullCancellableRateLimit)
        return
    }

    val rateLimitReference = this.rateLimitReference
    if (rateLimitReference == null) {
        block(NullCancellableRateLimit)
        return
    }

    val group = rateLimitReference.group
    val rateLimitInfo = context.getService<RateLimitContainer>()[group]
        ?: run {
            KotlinLogging.loggerOf<ComponentsListener>()
                .warn { "Could not find a rate limiter named '$group'" }
            val defaultMessages = context.getService<DefaultMessagesFactory>().get(event)
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

private suspend inline fun runRateLimited(noinline block: suspend (CancellableRateLimit) -> Boolean, bucket: Bucket) {
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