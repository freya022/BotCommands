package io.github.freya022.botcommands.api.core.utils

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.localization.DefaultMessages
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
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
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

suspend fun Guild.retrieveMemberOrNull(userId: Long): Member? = retrieveMemberOrNull(UserSnowflake.fromId(userId))
suspend fun Guild.retrieveMemberOrNull(user: UserSnowflake): Member? = runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_MEMBER) {
    retrieveMember(user).await()
}

suspend fun JDA.retrieveUserOrNull(userId: Long): User? = runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_USER) {
    retrieveUserById(userId).await()
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
 * Retrieves a thread by ID in this channel.
 *
 * The cached threads are checked first, and then requests are made to search in archived public threads,
 * and archived (but joined) private threads.
 *
 * @throws InsufficientPermissionException If the bot does not have the [Permission.MESSAGE_HISTORY] permission in this channel
 *
 * @see Guild.getThreadChannelById
 * @see IThreadContainer.retrieveArchivedPublicThreadChannels
 * @see IThreadContainer.retrieveArchivedPrivateJoinedThreadChannels
 */
suspend fun IThreadContainer.retrieveThreadChannelByIdOrNull(id: Long): ThreadChannel? {
    // Non-archived threads
    guild.getThreadChannelById(id)?.let { return it }

    // Archived public threads of the current channel
    retrieveArchivedPublicThreadChannels()
        .skipTo(id - 1)
        .limit(2) //Min limit is 2
        .await()
        .firstOrNull { it.idLong == id }
        ?.let { return it }

    // Archived, joined, private threads of the current channel
    retrieveArchivedPrivateJoinedThreadChannels()
        .skipTo(id - 1)
        .limit(2) //Min limit is 2
        .await()
        .firstOrNull { it.idLong == id }
        ?.let { return it }

    return null
}

/**
 * Awaits the completion of this RestAction.
 */
suspend fun RestAction<*>.awaitUnit() {
    submit().await()
}

/**
 * Awaits the completion of this RestAction and returns `null`.
 */
suspend fun <R> RestAction<*>.awaitNull(): R? {
    submit().await()
    return null
}

//region Send / Edit / Replace extensions
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
//endregion

/**
 * Deletes the original message using the hook after the specified delay.
 *
 * **Note:** This delays the rest action by the given delay.
 */
//TODO (maybe?) rename to deleteAfter when DeferrableInteractionCallbackAction is released
fun <R> RestAction<R>.deleteDelayed(hook: InteractionHook, delay: Duration): RestAction<R> =
    delay(delay).onSuccess { hook.deleteOriginal() }

/**
 * Deletes the message after the specified delay.
 *
 * **Note:** This delays the rest action by the given delay.
 */
//TODO (maybe?) rename to deleteAfter when DeferrableInteractionCallbackAction is released
fun RestAction<Message>.deleteDelayed(delay: Duration): RestAction<Message> =
    delay(delay).onSuccess(Message::delete)

// NOTE: Extensions of other RestAction execution methods using Kotlin Duration are omitted
//       as coroutines already enable the same behavior using `delay`

//region Duration extensions
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
 * @see RestAction.timeout
 */
@Suppress("UNCHECKED_CAST")
fun <T : RestAction<*>> T.timeout(duration: Duration): T =
    timeout(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS) as T

/**
 * @see JDA.awaitShutdown
 */
fun JDA.awaitShutdown(timeout: Duration): Boolean = awaitShutdown(timeout.toJavaDuration())

/**
 * @see Guild.timeoutFor
 */
fun Guild.timeoutFor(user: UserSnowflake, duration: Duration): AuditableRestAction<Void> = timeoutFor(user, duration.toJavaDuration())
/**
 * @see Member.timeoutFor
 */
fun Member.timeoutFor(duration: Duration): AuditableRestAction<Void> = timeoutFor(duration.toJavaDuration())

/**
 * @see TimeFormat.after
 */
fun TimeFormat.after(duration: Duration): Timestamp = after(duration.toJavaDuration())
/**
 * @see TimeFormat.before
 */
fun TimeFormat.before(duration: Duration): Timestamp = before(duration.toJavaDuration())

/**
 * @see Timestamp.plus
 */
operator fun Timestamp.plus(duration: Duration): Timestamp = plus(duration.toJavaDuration())
/**
 * @see Timestamp.minus
 */
operator fun Timestamp.minus(duration: Duration): Timestamp = minus(duration.toJavaDuration())

/**
 * @see Task.setTimeout
 */
fun <T> Task<T>.setTimeout(duration: Duration): Task<T> = setTimeout(duration.toJavaDuration())
/**
 * @see GatewayTask.setTimeout
 */
fun <T> GatewayTask<T>.setTimeout(duration: Duration): Task<T> = setTimeout(duration.toJavaDuration())
//endregion