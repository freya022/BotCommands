package com.freya02.botcommands.api.core.utils

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.entities.ReceivedMessage

suspend fun Guild.retrieveMemberOrNull(userId: Long): Member? = retrieveMemberOrNull(UserSnowflake.fromId(userId))
suspend fun Guild.retrieveMemberOrNull(user: UserSnowflake): Member? = try {
    retrieveMember(user).await()
} catch (e: ErrorResponseException) {
    if (e.errorResponse != ErrorResponse.UNKNOWN_MEMBER)
        throw e
    null
}

suspend fun JDA.retrieveUserOrNull(userId: Long): User? = try {
    retrieveUserById(userId).await()
} catch (e: ErrorResponseException) {
    if (e.errorResponse != ErrorResponse.UNKNOWN_MEMBER)
        throw e
    null
}

/**
 * Temporarily suppresses message content intent warnings
 *
 * **Note:** This applies to all threads while the current code is inside this function
 */
inline fun <R> suppressContentWarning(block: () -> R): R {
    val oldFlag = ReceivedMessage.didContentIntentWarning
    ReceivedMessage.didContentIntentWarning = true

    return try {
        block()
    } finally {
        ReceivedMessage.didContentIntentWarning = oldFlag
    }
}

/**
 * @see MessageEditData.fromCreateData
 */
fun MessageCreateData.toEditData() =
    MessageEditData.fromCreateData(this)
/**
 * @see IMessageEditCallback.editMessage
 */
fun MessageEditData.edit(callback: IMessageEditCallback) =
    callback.editMessage(this)

/**
 * @see MessageCreateData.fromEditData
 */
fun MessageEditData.toCreateData() =
    MessageCreateData.fromEditData(this)
/**
 * @see IReplyCallback.reply
 */
fun MessageCreateData.edit(callback: IReplyCallback) =
    callback.reply(this)