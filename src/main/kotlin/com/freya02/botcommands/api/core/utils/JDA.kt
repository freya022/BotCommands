package com.freya02.botcommands.api.core.utils

import com.freya02.botcommands.api.DefaultMessages
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.entities.ReceivedMessage
import java.util.*
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