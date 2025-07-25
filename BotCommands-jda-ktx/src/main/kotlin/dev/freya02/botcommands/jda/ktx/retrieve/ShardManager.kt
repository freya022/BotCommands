package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.requests.awaitOrNullOn
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.sharding.ShardManager

/**
 * Same as [ShardManager.retrieveUserById], but returns `null` on [ErrorResponse.UNKNOWN_USER].
 */
suspend fun ShardManager.retrieveUserByIdOrNull(id: Long): User? {
    return retrieveUserById(id).awaitOrNullOn(ErrorResponse.UNKNOWN_USER)
}

/**
 * Same as [ShardManager.retrieveUserById], but returns `null` on [ErrorResponse.UNKNOWN_USER].
 */
suspend fun ShardManager.retrieveUserByIdOrNull(id: String): User? {
    return retrieveUserById(id).awaitOrNullOn(ErrorResponse.UNKNOWN_USER)
}
