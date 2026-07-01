package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.IgnoreForMatch
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.deferredRestAction
import dev.freya02.botcommands.jda.ktx.requests.runIgnoringResponseOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Entitlement
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.entities.sticker.StickerUnion
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.RestActionImpl

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
suspend fun JDA.retrieveUserByIdOrNull(userId: String, @IgnoreForMatch useCache: Boolean = true): User? {
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
suspend fun JDA.retrieveUserByIdOrNull(userId: Long, @IgnoreForMatch useCache: Boolean = true): User? {
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
 * Retrieves a thread from any guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The [RestAction] may throw [InvalidChannelTypeException] if a channel with the ID was found, but isn't a thread.
 *
 * @see JDA.retrieveThreadChannelByIdOrNull
 */
fun JDA.retrieveThreadChannelById(id: Long): CacheRestAction<ThreadChannel> {
    return deferredRestAction(
        valueSupplier = { getThreadChannelById(id) },
        actionSupplier = {
            RestActionImpl(this, Route.Channels.GET_CHANNEL.compile(id.toString())) { res, _ ->
                val json = res.`object`
                val channelType = json.getInt("type").let(ChannelType::fromId)
                if (!channelType.isThread)
                    throw InvalidChannelTypeException("Invalid channel type, expected a thread, got $channelType")

                val guildId = json.getUnsignedLong("guild_id")
                (this as JDAImpl).entityBuilder.createThreadChannel(null, json, guildId, false)
            }
        }
    )
}

/**
 * Retrieves a thread from any guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The [RestAction] may throw [InvalidChannelTypeException] if a channel with the ID was found, but isn't a thread.
 *
 * @see JDA.retrieveThreadChannelByIdOrNull
 */
fun JDA.retrieveThreadChannelById(id: String): CacheRestAction<ThreadChannel> {
    return retrieveThreadChannelById(MiscUtil.parseSnowflake(id))
}

/**
 * Retrieves a thread from any guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The returned thread may be `null` if:
 * - It doesn't exist
 * - The bot doesn't have access to it
 * - The channel isn't a thread
 *
 * @see JDA.retrieveThreadChannelById
 */
suspend fun JDA.retrieveThreadChannelByIdOrNull(id: Long): ThreadChannel? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_CHANNEL, ErrorResponse.MISSING_ACCESS) {
        try {
            retrieveThreadChannelById(id).await()
        } catch (_: InvalidChannelTypeException) {
            return null
        }
    }
}

/**
 * Retrieves a thread from any guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The returned thread may be `null` if:
 * - It doesn't exist
 * - The bot doesn't have access to it
 * - The channel isn't a thread
 *
 * @see JDA.retrieveThreadChannelById
 */
suspend fun JDA.retrieveThreadChannelByIdOrNull(id: String): ThreadChannel? {
    return retrieveThreadChannelByIdOrNull(MiscUtil.parseSnowflake(id))
}
