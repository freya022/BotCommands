@file:Suppress("removal", "DEPRECATION")

package io.github.freya022.botcommands.api.core.utils

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.InlineMessage
import dev.freya02.botcommands.jda.ktx.messages.MessageCreate
import dev.freya02.botcommands.jda.ktx.messages.MessageEdit
import io.github.freya022.botcommands.api.core.annotations.ExperimentalCoreApi
import io.github.freya022.botcommands.api.core.exceptions.InvalidChannelTypeException
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.internal.utils.deferredRestAction
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Guild.Ban
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.entities.sticker.StickerUnion
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.*
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.Timestamp
import net.dv8tion.jda.api.utils.concurrent.Task
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.ReceivedMessage
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * Retrieves a [Member] with the provided ID.
 *
 * Throws the same exceptions as [Guild.retrieveMemberById],
 * minus [ErrorResponse.UNKNOWN_MEMBER] and [ErrorResponse.UNKNOWN_USER].
 *
 * @param userId   The ID of the member to retrieve
 * @param useCache Whether this should rely on the cache, set to `false` to always make a request.
 *
 * @see Guild.retrieveMemberById
 */
suspend fun Guild.retrieveMemberByIdOrNull(userId: String, useCache: Boolean = true): Member? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_MEMBER, ErrorResponse.UNKNOWN_USER) {
        retrieveMemberById(userId).useCache(useCache).await()
    }
}

/**
 * Retrieves a [Member] with the provided ID.
 *
 * Throws the same exceptions as [Guild.retrieveMemberById],
 * minus [ErrorResponse.UNKNOWN_MEMBER] and [ErrorResponse.UNKNOWN_USER].
 *
 * @param userId   The ID of the member to retrieve
 * @param useCache Whether this should rely on the cache, set to `false` to always make a request.
 *
 * @see Guild.retrieveMemberById
 */
suspend fun Guild.retrieveMemberByIdOrNull(userId: Long, useCache: Boolean = true): Member? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_MEMBER, ErrorResponse.UNKNOWN_USER) {
        retrieveMemberById(userId).useCache(useCache).await()
    }
}

/**
 * Retrieves a [Member] with the provided [User].
 *
 * Throws the same exceptions as [Guild.retrieveMember],
 * minus [ErrorResponse.UNKNOWN_MEMBER] and [ErrorResponse.UNKNOWN_USER].
 *
 * @param user     The user to retrieve the member for
 * @param useCache Whether this should rely on the cache, set to `false` to always make a request.
 *
 * @see Guild.retrieveMember
 */
suspend fun Guild.retrieveMemberOrNull(user: UserSnowflake, useCache: Boolean = true): Member? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_MEMBER, ErrorResponse.UNKNOWN_USER) {
        retrieveMember(user).useCache(useCache).await()
    }
}

/**
 * Retrieves a [Ban] of the provided [UserSnowflake], or `null` if the user is not banned.
 *
 * Throws the same exceptions as [Guild.retrieveBan], minus [ErrorResponse.UNKNOWN_BAN].
 *
 * @see Guild.retrieveBan
 */
suspend fun Guild.retrieveBanOrNull(user: UserSnowflake): Ban? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_BAN) {
        retrieveBan(user).await()
    }
}

/**
 * Retrieves the Vanity Invite meta-data for this guild,
 * or `null` if the vanity code is `null` or when a `INVITE_CODE_INVALID` error response was caught.
 *
 * Throws the same exceptions as [Guild.retrieveVanityInvite], minus [ErrorResponse.INVITE_CODE_INVALID].
 *
 * @see Guild.retrieveVanityInvite
 */
suspend fun Guild.retrieveVanityInviteOrNull(): VanityInvite? {
    if (vanityCode == null) return null

    return runIgnoringResponseOrNull(ErrorResponse.INVITE_CODE_INVALID) {
        retrieveVanityInvite().await()
    }
}

/**
 * Retrieves a [User] with the provided ID.
 *
 * Throws the same exceptions as [JDA.retrieveUserById], minus [ErrorResponse.UNKNOWN_USER].
 *
 * @param userId   ID of the user to retrieve
 * @param useCache Whether this should rely on the cache, set to `false` to always make a request.
 *
 * @see JDA.retrieveUserById
 */
suspend fun JDA.retrieveUserByIdOrNull(userId: String, useCache: Boolean = true): User? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_USER) {
        retrieveUserById(userId).useCache(useCache).await()
    }
}

/**
 * Retrieves a [User] with the provided ID.
 *
 * Throws the same exceptions as [JDA.retrieveUserById], minus [ErrorResponse.UNKNOWN_USER].
 *
 * @param userId   ID of the user to retrieve
 * @param useCache Whether this should rely on the cache, set to `false` to always make a request.
 *
 * @see JDA.retrieveUserById
 */
suspend fun JDA.retrieveUserByIdOrNull(userId: Long, useCache: Boolean = true): User? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_USER) {
        retrieveUserById(userId).useCache(useCache).await()
    }
}

/**
 * Retrieves a sticker from the provided ID, see [JDA.retrieveSticker] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveSticker], minus [ErrorResponse.UNKNOWN_STICKER].
 *
 * @see JDA.retrieveSticker
 */
suspend fun JDA.retrieveStickerOrNull(sticker: StickerSnowflake): StickerUnion? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_STICKER) {
        retrieveSticker(sticker).await()
    }
}

/**
 * Retrieves an [Entitlement] from the provided ID, see [JDA.retrieveEntitlementById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveEntitlementById], minus [ErrorResponse.UNKNOWN_ENTITLEMENT].
 *
 * @see JDA.retrieveEntitlementById
 */
suspend fun JDA.retrieveEntitlementByIdOrNull(entitlementId: String): Entitlement? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_ENTITLEMENT) {
        retrieveEntitlementById(entitlementId).await()
    }
}

/**
 * Retrieves an [Entitlement] from the provided ID, see [JDA.retrieveEntitlementById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveEntitlementById], minus [ErrorResponse.UNKNOWN_ENTITLEMENT].
 *
 * @see JDA.retrieveEntitlementById
 */
suspend fun JDA.retrieveEntitlementByIdOrNull(entitlementId: Long): Entitlement? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_ENTITLEMENT) {
        retrieveEntitlementById(entitlementId).await()
    }
}

/**
 * Retrieves a [Webhook] from the provided ID, see [JDA.retrieveWebhookById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveWebhookById], minus [ErrorResponse.UNKNOWN_WEBHOOK].
 *
 * @see JDA.retrieveWebhookById
 */
suspend fun JDA.retrieveWebhookByIdOrNull(webhookId: String): Webhook? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_WEBHOOK) {
        retrieveWebhookById(webhookId).await()
    }
}

/**
 * Retrieves a [Webhook] from the provided ID, see [JDA.retrieveWebhookById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveWebhookById], minus [ErrorResponse.UNKNOWN_WEBHOOK].
 *
 * @see JDA.retrieveWebhookById
 */
suspend fun JDA.retrieveWebhookByIdOrNull(webhookId: Long): Webhook? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_WEBHOOK) {
        retrieveWebhookById(webhookId).await()
    }
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
 * if you plan on showing them, be sure to use [PermissionLocalization.localize].
 *
 * @see PermissionLocalization.localize
 */
fun getMissingPermissions(requiredPerms: EnumSet<Permission>, permissionHolder: IPermissionHolder, channel: GuildChannel): Set<Permission> =
    EnumSet.copyOf(requiredPerms).also { it.removeAll(permissionHolder.getPermissions(channel)) }

/**
 * Retrieves a thread by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The [RestAction] may throw [InvalidChannelTypeException] if a channel with the ID was found, but isn't a thread.
 *
 * @see retrieveThreadChannelOrNull
 */
fun Guild.retrieveThreadChannelById(id: Long): CacheRestAction<ThreadChannel> {
    return jda.deferredRestAction(
        valueSupplier = { getThreadChannelById(id) },
        actionSupplier = {
            RestActionImpl(jda, Route.Channels.GET_CHANNEL.compile(id.toString())) { res, _ ->
                val dataObject = res.`object`
                val channelType = dataObject.getInt("type").let(ChannelType::fromId)
                if (!channelType.isThread)
                    throw InvalidChannelTypeException("Invalid channel type, expected a thread, got $channelType")

                (jda as JDAImpl).entityBuilder.createThreadChannel(this as GuildImpl, dataObject, this.idLong, false)
            }
        }
    )
}

/**
 * Retrieves a thread by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The returned thread may be null if:
 * - It doesn't exist
 * - The bot doesn't have access to it
 * - The channel isn't a thread
 *
 * @see retrieveThreadChannelById
 */
suspend fun Guild.retrieveThreadChannelOrNull(id: Long): ThreadChannel? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_CHANNEL, ErrorResponse.MISSING_ACCESS) {
        try {
            retrieveThreadChannelById(id).await()
        } catch (e: InvalidChannelTypeException) {
            return null
        }
    }
}

//region RestAction extensions
/**
 * Awaits the completion of this RestAction.
 */
suspend fun RestAction<*>.awaitUnit() {
    await()
}

/**
 * Awaits the completion of this RestAction and returns `null`.
 */
suspend fun <R> RestAction<*>.awaitNull(): R? {
    await()
    return null
}

/**
 * Queues this REST action while ignoring the provided error responses.
 *
 * Any other error will be handled using the [default failure consumer][RestAction.setDefaultFailure].
 *
 * @see ErrorHandler
 */
fun RestAction<*>.queueIgnoring(vararg errorResponses: ErrorResponse) {
    queue(null, ErrorHandler().ignore(errorResponses.asList()))
}

/**
 * Awaits the completion of this RestAction,
 * returns `null` if one of the provided error responses was thrown.
 *
 * Any other exception or error response occurs will be thrown.
 *
 * @see runIgnoringResponseOrNull
 */
suspend fun <R> RestAction<R>.awaitOrNullOn(vararg errorResponses: ErrorResponse): R? {
    return runIgnoringResponseOrNull(*errorResponses) {
        await()
    }
}

/**
 * Awaits the completion of this RestAction and wraps it in a Result.
 */
suspend fun <R> RestAction<R>.awaitCatching(): RestResult<R> {
    return runCatchingRest { await() }
}
//endregion

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
fun MessageCreateData.send(callback: IReplyCallback, ephemeral: Boolean = false): ReplyCallbackAction =
    callback.reply(this).setEphemeral(ephemeral)

/**
 * @see InteractionHook.sendMessage
 */
fun MessageCreateData.send(hook: InteractionHook, ephemeral: Boolean = false): WebhookMessageCreateAction<Message> =
    hook.sendMessage(this).setEphemeral(ephemeral)

/**
 * @see MessageChannel.sendMessage
 */
fun MessageCreateData.send(channel: MessageChannel): MessageCreateAction =
    channel.sendMessage(this)

/**
 * @see InteractionHook.editOriginal
 */
fun MessageEditData.edit(hook: InteractionHook): WebhookMessageEditAction<Message> =
    hook.editOriginal(this)

/**
 * @see MessageChannel.editMessageById
 */
fun MessageEditData.edit(channel: MessageChannel, id: Long): MessageEditAction =
    channel.editMessageById(id, this)

/**
 * @see IReplyCallback.reply
 */
@ExperimentalCoreApi // Unsure if jda-ktx will provide the `block` parameter to its own reply functions in the near future
inline fun IReplyCallback.reply(ephemeral: Boolean = false, block: InlineMessage<*>.() -> Unit): ReplyCallbackAction {
    return reply(MessageCreate { block() }).setEphemeral(ephemeral)
}

/**
 * @see IMessageEditCallback.editMessage
 */
@ExperimentalCoreApi // Unsure if jda-ktx will provide the `block` parameter to its own reply functions in the near future
inline fun IMessageEditCallback.edit(block: InlineMessage<*>.() -> Unit): MessageEditCallbackAction {
    return editMessage(MessageEdit { block() })
}

/**
 * @see InteractionHook.sendMessage
 */
@ExperimentalCoreApi // Unsure if jda-ktx will provide the `block` parameter to its own reply functions in the near future
inline fun InteractionHook.send(ephemeral: Boolean = false, block: InlineMessage<*>.() -> Unit): WebhookMessageCreateAction<Message> {
    return sendMessage(MessageCreate { block() }).setEphemeral(ephemeral)
}

/**
 * @see InteractionHook.editOriginal
 */
@ExperimentalCoreApi // Unsure if jda-ktx will provide the `block` parameter to its own reply functions in the near future
inline fun InteractionHook.edit(block: InlineMessage<*>.() -> Unit): WebhookMessageEditAction<Message> {
    return editOriginal(MessageEdit { block() })
}

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
fun <R> RestAction<R>.deleteDelayed(hook: InteractionHook, delay: Duration?): RestAction<R> {
    return withFiniteDelayOrSelf(delay) { finiteDelay ->
        delay(finiteDelay).flatMapOriginal { hook.deleteOriginal() }
    }
}

/**
 * Deletes the original message using the hook after the specified delay.
 *
 * **Note:** This delays the rest action by the given delay.
 */
fun RestAction<InteractionHook>.deleteDelayed(delay: Duration?): RestAction<InteractionHook> {
    return withFiniteDelayOrSelf(delay) { finiteDelay ->
        delay(finiteDelay).flatMapOriginal(InteractionHook::deleteOriginal)
    }
}

/**
 * Deletes the message after the specified delay.
 *
 * **Note:** This delays the rest action by the given delay.
 */
@JvmName("deleteDelayedMessage")
fun RestAction<Message>.deleteDelayed(delay: Duration?): RestAction<Message> {
    return withFiniteDelayOrSelf(delay) { finiteDelay ->
        delay(finiteDelay).flatMapOriginal(Message::delete)
    }
}

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

private inline fun <R : RestAction<*>> R.withFiniteDelayOrSelf(delay: Duration?, block: (finiteDelay: Duration) -> R): R {
    val finiteDelay = delay?.takeIfFinite() ?: return this
    return block(finiteDelay)
}

private fun <T> RestAction<T>.flatMapOriginal(block: (T) -> RestAction<*>): RestAction<T> {
    return flatMap { original -> block(original).map { original } }
}

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
 * @see Member.ban
 */
fun Member.ban(deletionTimeframe: Duration): AuditableRestAction<Void> =
    ban(deletionTimeframe.inWholeSeconds.toInt(), TimeUnit.SECONDS)

/**
 * @see Guild.ban
 */
fun Guild.ban(user: UserSnowflake, deletionTimeframe: Duration): AuditableRestAction<Void> =
    ban(user, deletionTimeframe.inWholeSeconds.toInt(), TimeUnit.SECONDS)

/**
 * See [bulk ban from JDA](https://docs.jda.wiki/net/dv8tion/jda/api/entities/Guild.html#ban(java.util.Collection,java.time.Duration))
 */
fun Guild.ban(users: Collection<UserSnowflake>, duration: Duration): AuditableRestAction<BulkBanResponse> =
    ban(users, duration.toJavaDuration())

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

/**
 * The minimum and maximum amount of values a user can select.
 */
fun <B : SelectMenu.Builder<*, B>> SelectMenu.Builder<*, B>.setRequiredRange(range: IntRange): B = setRequiredRange(range.first, range.last)
