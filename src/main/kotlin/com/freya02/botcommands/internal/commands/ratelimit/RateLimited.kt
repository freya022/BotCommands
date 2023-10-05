package com.freya02.botcommands.internal.commands.ratelimit

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import com.freya02.botcommands.api.commands.ratelimit.RateLimitContainer
import com.freya02.botcommands.api.commands.ratelimit.RateLimitInfo
import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.components.data.ComponentData
import io.github.bucket4j.Bucket
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

internal interface RateLimited {
    val path: CommandPath
    val rateLimitInfo: RateLimitInfo?
}

internal suspend fun TextCommandInfo.withRateLimit(context: BContext, event: MessageReceivedEvent, isNotOwner: Boolean, block: suspend (CancellableRateLimit) -> Boolean) {
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

internal suspend fun ApplicationCommandInfo.withRateLimit(context: BContext, event: GenericCommandInteractionEvent, isNotOwner: Boolean, block: suspend (CancellableRateLimit) -> Boolean) {
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

internal suspend fun ComponentData.withRateLimit(context: BContext, event: GenericComponentInteractionCreateEvent, isNotOwner: Boolean, block: suspend (CancellableRateLimit) -> Boolean) {
    val rateLimitInfo = this.rateLimitGroup?.let { context.getService<RateLimitContainer>()[it] }
    if (isNotOwner && rateLimitInfo != null) {
        val bucket = rateLimitInfo.limiter.getBucket(context, event)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.limiter.onRateLimit(context, event, probe)
        }
    } else {
        block(NullCancellableRateLimit)
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