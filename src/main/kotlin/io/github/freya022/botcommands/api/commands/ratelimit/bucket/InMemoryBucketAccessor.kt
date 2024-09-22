package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.local.LocalBucket
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger { }

/**
 * Default in-memory [BucketAccessor] implementation using [RateLimitScope].
 *
 * **Note:** The rate limit scopes using guilds or channels are limited to guild-only events,
 * a user rate limit is applied if the limitation is violated.
 *
 * @param scope                 Scope of the rate limit, see [RateLimitScope] values.
 * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
 */
class InMemoryBucketAccessor(
    private val scope: RateLimitScope,
    private val configurationSupplier: BucketConfigurationSupplier
) : BucketAccessor {

    @JvmRecord
    private data class RateLimitKey(private val placeId: Long?, private val userId: Long?) {
        init {
            if (placeId == null && userId == null)
                throwInternal("Rate limiting cannot be done on an empty key")
        }
    }

    private val map: MutableMap<RateLimitKey, Bucket> = ConcurrentHashMap()

    override suspend fun getBucket(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo): Bucket {
        return map.computeIfAbsent(event.toRateLimitKey()) {
            configurationSupplier.getConfiguration(context, event, commandInfo).toBucket()
        }
    }

    private fun MessageReceivedEvent.toRateLimitKey(): RateLimitKey {
        if (!isFromGuild) throwInternal("Invalid rate limit scope for text commands")
        return when (scope) {
            RateLimitScope.USER -> RateLimitKey(null, author.idLong)
            RateLimitScope.USER_PER_GUILD -> RateLimitKey(guild.idLong, author.idLong)
            RateLimitScope.USER_PER_CHANNEL -> RateLimitKey(channel.idLong, author.idLong)
            RateLimitScope.GUILD -> RateLimitKey(guild.idLong, null)
            RateLimitScope.CHANNEL -> RateLimitKey(channel.idLong, null)
        }
    }

    override suspend fun getBucket(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Bucket {
        return map.computeIfAbsent(event.toRateLimitKey()) {
            configurationSupplier.getConfiguration(context, event, commandInfo).toBucket()
        }
    }

    override suspend fun getBucket(context: BContext, event: GenericComponentInteractionCreateEvent): Bucket {
        return map.computeIfAbsent(event.toRateLimitKey()) {
            configurationSupplier.getConfiguration(context, event).toBucket()
        }
    }

    private fun BucketConfiguration.toBucket(): LocalBucket {
        return Bucket.builder()
            .apply { bandwidths.forEach(::addLimit) }
            .build()
    }

    private fun GenericInteractionCreateEvent.toRateLimitKey(): RateLimitKey {
        return when (scope) {
            RateLimitScope.USER -> RateLimitKey(null, user.idLong)
            RateLimitScope.USER_PER_GUILD -> {
                val guild = guild ?: return fallbackUserKey(user)
                RateLimitKey(guild.idLong, user.idLong)
            }
            RateLimitScope.USER_PER_CHANNEL -> {
                if (isFromGuild) RateLimitKey(guildChannel.idLong, user.idLong) else fallbackUserKey(user)
            }
            RateLimitScope.GUILD -> {
                val guild = guild ?: return fallbackUserKey(user)
                RateLimitKey(guild.idLong, null)
            }
            RateLimitScope.CHANNEL -> {
                if (isFromGuild) RateLimitKey(guildChannel.idLong, null) else fallbackUserKey(user)
            }
        }
    }

    private fun fallbackUserKey(user: UserSnowflake): RateLimitKey {
        logger.warn {
            "Tried to get an invalid rate limit bucket, rate limiters outside of guilds must only use the ${RateLimitScope.USER} scope. " +
                    "Returning an user bucket instead."
        }
        return RateLimitKey(null, user.idLong)
    }
}