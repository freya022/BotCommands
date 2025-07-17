package dev.freya02.botcommands.jda.ktx

import io.github.freya022.botcommands.api.core.waiter.EventWaiter
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Suspends until a [Message] from the [author] satisfying the [filter] is received on any shard, then returns it.
 *
 * If you wish to use a timeout with it, you can use [withTimeoutOrNull][kotlinx.coroutines.withTimeoutOrNull].
 *
 * @param filter Additional conditions to satisfy before returning the message
 */
suspend inline fun EventWaiter.awaitMessage(
    channel: MessageChannel,
    author: UserSnowflake? = null,
    crossinline filter: (Message) -> Boolean = { true }
): Message {
    val channelId = channel.idLong
    val authorId = author?.idLong

    return await<MessageReceivedEvent> {
        it.channel.idLong == channelId
                && (authorId == null || it.author.idLong == authorId)
                && filter(it.message)
    }.message
}
