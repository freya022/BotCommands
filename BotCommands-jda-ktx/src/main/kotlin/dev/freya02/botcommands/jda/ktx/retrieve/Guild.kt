package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.deferredRestAction
import dev.freya02.botcommands.jda.ktx.requests.runIgnoringResponseOrNull
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Guild.Ban
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.requests.RestActionImpl

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
@DeprecatedInBcCore
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
@DeprecatedInBcCore
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
@DeprecatedInBcCore
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
@DeprecatedInBcCore
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
@DeprecatedInBcCore
suspend fun Guild.retrieveVanityInviteOrNull(): VanityInvite? {
    if (vanityCode == null) return null

    return runIgnoringResponseOrNull(ErrorResponse.INVITE_CODE_INVALID) {
        retrieveVanityInvite().await()
    }
}

/**
 * Retrieves a thread by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The [RestAction] may throw [InvalidChannelTypeException] if a channel with the ID was found, but isn't a thread.
 *
 * @see retrieveThreadChannelOrNull
 */
@DeprecatedInBcCore
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
 * The [RestAction] may throw [InvalidChannelTypeException] if a channel with the ID was found, but isn't a thread.
 *
 * @see retrieveThreadChannelOrNull
 */
fun Guild.retrieveThreadChannelById(id: String): CacheRestAction<ThreadChannel> {
    return retrieveThreadChannelById(MiscUtil.parseSnowflake(id))
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
@DeprecatedInBcCore
@Deprecated("Replaced by retrieveThreadChannelByIdOrNull")
@Suppress("deprecated")
suspend fun Guild.retrieveThreadChannelOrNull(id: Long): ThreadChannel? {
    return retrieveThreadChannelByIdOrNull(id)
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
suspend fun Guild.retrieveThreadChannelByIdOrNull(id: Long): ThreadChannel? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_CHANNEL, ErrorResponse.MISSING_ACCESS) {
        try {
            retrieveThreadChannelById(id).await()
        } catch (_: InvalidChannelTypeException) {
            return null
        }
    }
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
suspend fun Guild.retrieveThreadChannelByIdOrNull(id: String): ThreadChannel? {
    return retrieveThreadChannelByIdOrNull(MiscUtil.parseSnowflake(id))
}
