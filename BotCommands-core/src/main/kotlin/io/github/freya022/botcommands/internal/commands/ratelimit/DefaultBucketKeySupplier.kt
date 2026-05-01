package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

private val logger = KotlinLogging.logger { }

class DefaultBucketKeySupplier internal constructor(val scope: RateLimitScope) : BucketKeySupplier {

    private val handlers = ServiceLoader.load(RequestHandler::class.java)

    override fun getKey(context: RateLimitingContext): BucketKeySupplier.Key {
        for (handler in handlers) {
            val key = handler.handle(this, context)
            if (key != null) {
                return key
            }
        }

        throwInternal("Unsupported context: ${context.javaClass.name}")
    }

    fun getRateLimitKey(event: Interaction, identifier: String): BucketKeySupplier.Key {
        if (scope.isGuild && !event.isFromGuild) {
            logger.warn { "Cannot get a bucket with the $scope scope outside of a guild, using the user ID instead." }
            return BucketKeySupplier.UserKey(identifier, event.user.idLong)
        }

        return when (scope) {
            RateLimitScope.USER -> BucketKeySupplier.UserKey(identifier, event.user.idLong)
            RateLimitScope.USER_PER_GUILD -> {
                val guild = event.guild ?: throwInternal("Guild should be present")
                BucketKeySupplier.UserAtPlaceKey(identifier, guild.idLong, event.user.idLong)
            }
            RateLimitScope.USER_PER_CHANNEL -> BucketKeySupplier.UserAtPlaceKey(
                identifier,
                event.channelIdLong,
                event.user.idLong
            )
            RateLimitScope.GUILD -> {
                val guild = event.guild ?: throwInternal("Guild should be present")
                BucketKeySupplier.PlaceKey(identifier, guild.idLong)
            }
            RateLimitScope.CHANNEL -> BucketKeySupplier.PlaceKey(identifier, event.channelIdLong)
        }
    }

    fun interface RequestHandler {
        fun handle(instance: DefaultBucketKeySupplier, context: RateLimitingContext): BucketKeySupplier.Key?
    }
}
