package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.IgnoreForMatch
import dev.freya02.botcommands.jda.ktx.requests.awaitOrNullOn
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.requests.ErrorResponse

/**
 * Same as [WebhookClient.retrieveMessageById], but returns `null` on [ErrorResponse.UNKNOWN_MESSAGE].
 */
suspend fun WebhookClient<*>.retrieveMessageByIdOrNull(id: String, @IgnoreForMatch threadId: String? = null): Message? {
    return retrieveMessageById(id).setThreadId(threadId).awaitOrNullOn(ErrorResponse.UNKNOWN_MESSAGE)
}
