package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.IgnoreForMatch
import dev.freya02.botcommands.jda.ktx.requests.awaitOrNullOn
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.ThreadMember
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.requests.ErrorResponse

/**
 * Same as [ThreadChannel.retrieveParentMessage], but returns `null` on [ErrorResponse.UNKNOWN_MESSAGE].
 */
suspend fun ThreadChannel.retrieveParentMessageOrNull(): Message? {
    return retrieveParentMessage().awaitOrNullOn(ErrorResponse.UNKNOWN_MESSAGE)
}

/**
 * Same as [ThreadChannel.retrieveStartMessage], but returns `null` on [ErrorResponse.UNKNOWN_MESSAGE].
 */
suspend fun ThreadChannel.retrieveStartMessageOrNull(): Message? {
    return retrieveStartMessage().awaitOrNullOn(ErrorResponse.UNKNOWN_MESSAGE)
}

/**
 * Same as [ThreadChannel.retrieveThreadMemberById], but returns `null` on [ErrorResponse.UNKNOWN_MEMBER].
 */
suspend fun ThreadChannel.retrieveThreadMemberOrNull(user: UserSnowflake, @IgnoreForMatch useCache: Boolean = true): ThreadMember? {
    return retrieveThreadMemberById(user.idLong).useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_MEMBER)
}

/**
 * Same as [ThreadChannel.retrieveThreadMemberById], but returns `null` on [ErrorResponse.UNKNOWN_MEMBER].
 */
suspend fun ThreadChannel.retrieveThreadMemberByIdOrNull(id: Long, @IgnoreForMatch useCache: Boolean = true): ThreadMember? {
    return retrieveThreadMemberById(id).useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_MEMBER)
}

/**
 * Same as [ThreadChannel.retrieveThreadMemberById], but returns `null` on [ErrorResponse.UNKNOWN_MEMBER].
 */
suspend fun ThreadChannel.retrieveThreadMemberByIdOrNull(id: String, @IgnoreForMatch useCache: Boolean = true): ThreadMember? {
    return retrieveThreadMemberById(id).useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_MEMBER)
}
