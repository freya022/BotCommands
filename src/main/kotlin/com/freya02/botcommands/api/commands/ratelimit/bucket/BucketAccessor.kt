package com.freya02.botcommands.api.commands.ratelimit.bucket

import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import io.github.bucket4j.Bucket
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Retrieves rate limit buckets based off the execution context.
 */
interface BucketAccessor {
    suspend fun getBucket(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo): Bucket

    suspend fun getBucket(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Bucket

    suspend fun getBucket(context: BContext, event: GenericComponentInteractionCreateEvent): Bucket
}