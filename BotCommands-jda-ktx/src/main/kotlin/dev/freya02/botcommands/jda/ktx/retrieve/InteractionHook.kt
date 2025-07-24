package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.requests.awaitOrNullOn
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.ErrorResponse

/**
 * Same as [InteractionHook.retrieveOriginal], but returns `null` on [ErrorResponse.UNKNOWN_MESSAGE].
 */
suspend fun InteractionHook.retrieveOriginalOrNull(): Message? {
    return retrieveOriginal().awaitOrNullOn(ErrorResponse.UNKNOWN_MESSAGE)
}
