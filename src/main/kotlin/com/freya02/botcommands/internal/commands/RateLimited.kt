package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.ratelimit.RateLimitInfo
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import io.github.bucket4j.Bucket
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

internal sealed interface RateLimited {
    val path: CommandPath
    val rateLimitInfo: RateLimitInfo?
}

internal suspend fun TextCommandInfo.withRateLimit(context: BContext, event: MessageReceivedEvent, isNotOwner: Boolean, block: suspend () -> Boolean) {
    val rateLimitInfo = rateLimitInfo
    if (isNotOwner && rateLimitInfo != null) {
        val bucket = rateLimitInfo.helper.getBucket(context, event, this)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.helper.onRateLimit(context, event, this, probe)
        }
    } else {
        block()
    }
}

internal suspend fun ApplicationCommandInfo.withRateLimit(context: BContext, event: GenericCommandInteractionEvent, isNotOwner: Boolean, block: suspend () -> Boolean) {
    val rateLimitInfo = rateLimitInfo
    if (isNotOwner && rateLimitInfo != null) {
        val bucket = rateLimitInfo.helper.getBucket(context, event, this)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.helper.onRateLimit(context, event, this, probe)
        }
    } else {
        block()
    }
}

private suspend inline fun runRateLimited(crossinline block: suspend () -> Boolean, bucket: Bucket) {
    try {
        block()
    } catch (e: Throwable) {
        bucket.addTokens(1)
        throw e
    }
}