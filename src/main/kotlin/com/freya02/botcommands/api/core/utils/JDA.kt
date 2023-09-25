package com.freya02.botcommands.api.core.utils

import com.freya02.botcommands.api.DefaultMessages
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.Timestamp
import net.dv8tion.jda.api.utils.concurrent.Task
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.entities.ReceivedMessage
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import kotlin.time.Duration
import kotlin.time.toJavaDuration

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
 * Computes the missing permissions from the specified permission holder,
 * If you plan on showing them, be sure to use [DefaultMessages.getPermission]
 *
 * @see DefaultMessages.getPermission
 */
fun getMissingPermissions(requiredPerms: EnumSet<Permission>, permissionHolder: IPermissionHolder, channel: GuildChannel): Set<Permission> =
    EnumSet.copyOf(requiredPerms).also { it.removeAll(permissionHolder.getPermissions(channel)) }

/**
 * @see MessageEditData.fromCreateData
 */
fun MessageCreateData.toEditData(): MessageEditData =
    MessageEditData.fromCreateData(this)
/**
 * @see IMessageEditCallback.editMessage
 */
fun MessageEditData.edit(callback: IMessageEditCallback): MessageEditCallbackAction =
    callback.editMessage(this)

/**
 * @see MessageCreateData.fromEditData
 */
fun MessageEditData.toCreateData(): MessageCreateData =
    MessageCreateData.fromEditData(this)
/**
 * @see IReplyCallback.reply
 */
fun MessageCreateData.edit(callback: IReplyCallback): ReplyCallbackAction =
    callback.reply(this)

/**
 * @see InteractionHook.sendMessage
 */
fun MessageCreateData.send(hook: InteractionHook): WebhookMessageCreateAction<Message> =
    hook.sendMessage(this)

/**
 * @see InteractionHook.editOriginal
 */
fun MessageEditData.edit(hook: InteractionHook): WebhookMessageEditAction<Message> =
    hook.editOriginal(this)

/**
 * @see InteractionHook.editOriginal
 */
fun InteractionHook.replaceWith(data: MessageEditData): WebhookMessageEditAction<Message> =
    editOriginal(data).setReplace(true)

/**
 * @see InteractionHook.editOriginal
 */
fun InteractionHook.replaceWith(data: MessageCreateData): WebhookMessageEditAction<Message> =
    editOriginal(data.toEditData()).setReplace(true)

/**
 * @see InteractionHook.editOriginal
 */
fun InteractionHook.replaceWith(content: String): WebhookMessageEditAction<Message> =
    editOriginal(content).setReplace(true)

/**
 * @see RestAction.delay
 */
fun <T> RestAction<T>.delay(duration: Duration): RestAction<T> =
    delay(duration.toJavaDuration())

/**
 * @see RestAction.delay
 */
fun <T> RestAction<T>.delay(duration: Duration, scheduler: ScheduledExecutorService): RestAction<T> =
    delay(duration.toJavaDuration(), scheduler)

/**
 * @see JDA.awaitShutdown
 */
fun JDA.awaitShutdown(timeout: Duration) = awaitShutdown(timeout.toJavaDuration())

/**
 * @see Guild.timeoutFor
 */
fun Guild.timeoutFor(user: UserSnowflake, duration: Duration) = timeoutFor(user, duration.toJavaDuration())
/**
 * @see Member.timeoutFor
 */
fun Member.timeoutFor(duration: Duration) = timeoutFor(duration.toJavaDuration())

/**
 * @see TimeFormat.after
 */
fun TimeFormat.after(duration: Duration) = after(duration.toJavaDuration())
/**
 * @see TimeFormat.before
 */
fun TimeFormat.before(duration: Duration) = before(duration.toJavaDuration())

/**
 * @see Timestamp.plus
 */
operator fun Timestamp.plus(duration: Duration) = plus(duration.toJavaDuration())
/**
 * @see Timestamp.minus
 */
operator fun Timestamp.minus(duration: Duration) = minus(duration.toJavaDuration())

/**
 * @see Task.setTimeout
 */
fun <T> Task<T>.setTimeout(duration: Duration) = setTimeout(duration.toJavaDuration())
/**
 * @see GatewayTask.setTimeout
 */
fun <T> GatewayTask<T>.setTimeout(duration: Duration) = setTimeout(duration.toJavaDuration())