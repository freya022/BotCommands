package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.local.LocalBucket
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope.*
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.uniqueCommandPath
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
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

    private sealed interface Key {
        override fun equals(other: Any?): Boolean
        override fun hashCode(): Int
    }

    private data class UserKey(private val identifier: String, private val id: Long) : Key
    private data class PlaceKey(private val identifier: String, private val id: Long) : Key
    private data class UserAtPlaceKey(private val identifier: String, private val placeId: Long, private val userId: Long) : Key

    private val map: MutableMap<Key, Bucket> = ConcurrentHashMap()

    override suspend fun getBucket(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo): Bucket {
        return map.computeIfAbsent(commandInfo.getRateLimitKey(event)) {
            configurationSupplier.getConfiguration(context, event, commandInfo).toBucket()
        }
    }

    private fun TextCommandInfo.getRateLimitKey(event: MessageReceivedEvent): Key {
        if (!event.isFromGuild) throwInternal("Invalid rate limit scope for text commands")
        return when (scope) {
            USER -> UserKey(path.fullPath, event.author.idLong)
            USER_PER_GUILD -> UserAtPlaceKey(path.fullPath, event.guild.idLong, event.author.idLong)
            USER_PER_CHANNEL -> UserAtPlaceKey(path.fullPath, event.channel.idLong, event.author.idLong)
            GUILD -> PlaceKey(path.fullPath, event.guild.idLong)
            CHANNEL -> PlaceKey(path.fullPath, event.channel.idLong)
        }
    }

    override suspend fun getBucket(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): Bucket {
        return map.computeIfAbsent(getRateLimitKey(event)) {
            configurationSupplier.getConfiguration(context, event, commandInfo).toBucket()
        }
    }

    override suspend fun getBucket(context: BContext, event: GenericComponentInteractionCreateEvent): Bucket {
        return map.computeIfAbsent(getRateLimitKey(event)) {
            configurationSupplier.getConfiguration(context, event).toBucket()
        }
    }

    private fun BucketConfiguration.toBucket(): LocalBucket {
        return Bucket.builder()
            .apply { bandwidths.forEach(::addLimit) }
            .build()
    }

    private fun getRateLimitKey(event: CommandInteraction): Key {
        return getRateLimitKey(event, event.uniqueCommandPath)
    }

    private fun getRateLimitKey(event: ComponentInteraction): Key {
        return getRateLimitKey(event, TODO())
    }

    private fun getRateLimitKey(event: Interaction, identifier: String): Key {
        if (scope.isGuild && !event.isFromGuild) {
            logger.warn { "Cannot get a bucket with the $scope scope outside of a guild, using the user ID instead." }
            return UserKey(identifier, event.user.idLong)
        }

        return when (scope) {
            USER -> UserKey(identifier, event.user.idLong)
            USER_PER_GUILD -> {
                val guild = event.guild ?: throwInternal("Guild should be present")
                UserAtPlaceKey(identifier, guild.idLong, event.user.idLong)
            }
            USER_PER_CHANNEL -> UserAtPlaceKey(identifier, event.channelIdLong, event.user.idLong)
            GUILD -> {
                val guild = event.guild ?: throwInternal("Guild should be present")
                PlaceKey(identifier, guild.idLong)
            }
            CHANNEL -> PlaceKey(identifier, event.channelIdLong)
        }
    }
}