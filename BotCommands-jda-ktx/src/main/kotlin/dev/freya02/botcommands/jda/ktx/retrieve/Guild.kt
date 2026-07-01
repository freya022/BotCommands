package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.IgnoreForMatch
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.deferredRestAction
import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import dev.freya02.botcommands.jda.ktx.requests.awaitOrNullOn
import dev.freya02.botcommands.jda.ktx.requests.onErrorResponseException
import dev.freya02.botcommands.jda.ktx.requests.runIgnoringResponseOrNull
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Guild.Ban
import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.entities.sticker.GuildSticker
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
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
suspend fun Guild.retrieveMemberByIdOrNull(userId: String, @IgnoreForMatch useCache: Boolean = true): Member? {
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
suspend fun Guild.retrieveMemberByIdOrNull(userId: Long, @IgnoreForMatch useCache: Boolean = true): Member? {
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
suspend fun Guild.retrieveMemberOrNull(user: UserSnowflake, @IgnoreForMatch useCache: Boolean = true): Member? {
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
 * Retrieves a thread belonging to this guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The [RestAction] may throw [InvalidChannelTypeException] if a channel with the ID was found, but isn't a thread.
 * It may also throw [ParentGuildMismatchException] if the channel isn't from the same guild.
 *
 * @see Guild.retrieveThreadChannelByIdOrNull
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

                if (dataObject.getUnsignedLong("guild_id", 0) != this.idLong) {
                    throw ParentGuildMismatchException("Thread $id is not from the same guild (expected ${this.id})")
                }

                (jda as JDAImpl).entityBuilder.createThreadChannel(this as GuildImpl, dataObject, this.idLong, false)
            }
        }
    )
}

/**
 * Retrieves a thread belonging to this guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The [RestAction] may throw [InvalidChannelTypeException] if a channel with the ID was found, but isn't a thread.
 * It may also throw [ParentGuildMismatchException] if the channel isn't from the same guild.
 *
 * @see Guild.retrieveThreadChannelByIdOrNull
 */
fun Guild.retrieveThreadChannelById(id: String): CacheRestAction<ThreadChannel> {
    return retrieveThreadChannelById(MiscUtil.parseSnowflake(id))
}

/**
 * Retrieves a thread belonging to this guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The returned thread may be `null` if:
 * - It doesn't exist
 * - The bot doesn't have access to it
 * - The channel isn't a thread
 * - The channel isn't from the correct guild
 *
 * @see Guild.retrieveThreadChannelById
 */
suspend fun Guild.retrieveThreadChannelByIdOrNull(id: Long): ThreadChannel? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_CHANNEL, ErrorResponse.MISSING_ACCESS) {
        try {
            retrieveThreadChannelById(id).await()
        } catch (_: InvalidChannelTypeException) {
            return null
        } catch (_: ParentGuildMismatchException) {
            return null
        }
    }
}

/**
 * Retrieves a thread belonging to this guild, by ID.
 *
 * The cached threads are checked first, and then a request is made.
 *
 * The returned thread may be `null` if:
 * - It doesn't exist
 * - The bot doesn't have access to it
 * - The channel isn't a thread
 * - The channel isn't from the correct guild
 *
 * @see Guild.retrieveThreadChannelById
 */
suspend fun Guild.retrieveThreadChannelByIdOrNull(id: String): ThreadChannel? {
    return retrieveThreadChannelByIdOrNull(MiscUtil.parseSnowflake(id))
}

/**
 * Same as [Guild.retrieveAutoModRuleById], but returns `null` if the rule does not exist.
 */
suspend fun Guild.retrieveAutoModRuleByIdOrNull(id: Long): AutoModRule? {
    return retrieveAutoModRuleByIdOrNull(id.toString())
}

/**
 * Same as [Guild.retrieveAutoModRuleById], but returns `null` if the rule does not exist.
 */
suspend fun Guild.retrieveAutoModRuleByIdOrNull(id: String): AutoModRule? {
    return retrieveAutoModRuleById(id)
        .awaitCatching()
        .onErrorResponseException {
            if (it.response.code == 404) return null
        }
        .getOrThrow()
}

/**
 * Same as [Guild.retrieveEmojiById], but returns `null` on [ErrorResponse.UNKNOWN_EMOJI].
 */
suspend fun Guild.retrieveEmojiByIdOrNull(id: Long): RichCustomEmoji? {
    return retrieveEmojiById(id).awaitOrNullOn(ErrorResponse.UNKNOWN_EMOJI)
}

/**
 * Same as [Guild.retrieveEmojiById], but returns `null` on [ErrorResponse.UNKNOWN_EMOJI].
 */
suspend fun Guild.retrieveEmojiByIdOrNull(id: String): RichCustomEmoji? {
    return retrieveEmojiById(id).awaitOrNullOn(ErrorResponse.UNKNOWN_EMOJI)
}

/**
 * Same as [Guild.retrieveEmoji], but returns `null` on [ErrorResponse.UNKNOWN_EMOJI].
 */
suspend fun Guild.retrieveEmojiOrNull(emoji: CustomEmoji): RichCustomEmoji? {
    return retrieveEmoji(emoji).awaitOrNullOn(ErrorResponse.UNKNOWN_EMOJI)
}

/**
 * Same as [Guild.retrieveSticker], but returns `null` on [ErrorResponse.UNKNOWN_STICKER].
 */
suspend fun Guild.retrieveStickerOrNull(sticker: StickerSnowflake): GuildSticker? {
    return retrieveSticker(sticker).awaitOrNullOn(ErrorResponse.UNKNOWN_STICKER)
}

/**
 * Same as [Guild.retrieveWelcomeScreen], but returns `null` on [ErrorResponse.UNKNOWN_GUILD_WELCOME_SCREEN].
 */
suspend fun Guild.retrieveWelcomeScreenOrNull(): GuildWelcomeScreen? {
    return retrieveWelcomeScreen().awaitOrNullOn(ErrorResponse.UNKNOWN_GUILD_WELCOME_SCREEN)
}

/**
 * Same as [Guild.retrieveMemberVoiceStateById], but returns `null` on [ErrorResponse.UNKNOWN_VOICE_STATE].
 */
suspend fun Guild.retrieveMemberVoiceStateByIdOrNull(id: Long, @IgnoreForMatch useCache: Boolean = true): GuildVoiceState? {
    return retrieveMemberVoiceStateById(id).useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_VOICE_STATE)
}

/**
 * Same as [Guild.retrieveMemberVoiceStateById], but returns `null` on [ErrorResponse.UNKNOWN_VOICE_STATE].
 */
suspend fun Guild.retrieveMemberVoiceStateByIdOrNull(id: String): GuildVoiceState? {
    return retrieveMemberVoiceStateById(id).awaitOrNullOn(ErrorResponse.UNKNOWN_VOICE_STATE)
}

/**
 * Same as [Guild.retrieveMemberVoiceState], but returns `null` on [ErrorResponse.UNKNOWN_VOICE_STATE].
 */
suspend fun Guild.retrieveMemberVoiceStateOrNull(user: UserSnowflake): GuildVoiceState? {
    return retrieveMemberVoiceState(user).awaitOrNullOn(ErrorResponse.UNKNOWN_VOICE_STATE)
}

/**
 * Same as [Guild.retrieveOwner], but returns `null` on [ErrorResponse.UNKNOWN_MEMBER] or [ErrorResponse.UNKNOWN_USER].
 */
suspend fun Guild.retrieveOwnerOrNull(@IgnoreForMatch useCache: Boolean = true): Member? {
    return retrieveOwner().useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_MEMBER, ErrorResponse.UNKNOWN_USER)
}

/**
 * Same as [Guild.retrieveScheduledEventById], but returns `null` on [ErrorResponse.UNKNOWN_SCHEDULED_EVENT].
 */
suspend fun Guild.retrieveScheduledEventByIdOrNull(id: Long, @IgnoreForMatch useCache: Boolean = true): ScheduledEvent? {
    return retrieveScheduledEventById(id).useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_SCHEDULED_EVENT)
}

/**
 * Same as [Guild.retrieveScheduledEventById], but returns `null` on [ErrorResponse.UNKNOWN_SCHEDULED_EVENT].
 */
suspend fun Guild.retrieveScheduledEventByIdOrNull(id: String, @IgnoreForMatch useCache: Boolean = true): ScheduledEvent? {
    return retrieveScheduledEventById(id).useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_SCHEDULED_EVENT)
}

/**
 * Same as [Guild.retrieveSoundboardSound], but returns `null` on [ErrorResponse.UNKNOWN_SOUND].
 */
suspend fun Guild.retrieveSoundboardSoundOrNull(soundboardSound: SoundboardSoundSnowflake, @IgnoreForMatch useCache: Boolean = true): SoundboardSound? {
    return retrieveSoundboardSound(soundboardSound).useCache(useCache).awaitOrNullOn(ErrorResponse.UNKNOWN_SOUND)
}
