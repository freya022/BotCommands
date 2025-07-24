package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.requests.awaitOrNullOn
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.requests.ErrorResponse

/**
 * Same as [MessageChannel.retrieveMessageById], but returns `null` on [ErrorResponse.UNKNOWN_MESSAGE].
 */
suspend fun MessageChannel.retrieveMessageByIdOrNull(id: Long): Message? {
    return retrieveMessageById(id).awaitOrNullOn(ErrorResponse.UNKNOWN_MESSAGE)
}

/**
 * Same as [MessageChannel.retrieveMessageById], but returns `null` on [ErrorResponse.UNKNOWN_MESSAGE].
 */
suspend fun MessageChannel.retrieveMessageByIdOrNull(id: String): Message? {
    return retrieveMessageById(id).awaitOrNullOn(ErrorResponse.UNKNOWN_MESSAGE)
}
