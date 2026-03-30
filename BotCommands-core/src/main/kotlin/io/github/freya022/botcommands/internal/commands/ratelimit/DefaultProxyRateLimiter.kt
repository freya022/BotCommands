package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.ratelimit.*
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope.*
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.*
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeySupplier.*
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import io.github.freya022.botcommands.api.commands.ratelimit.handler.RateLimitHandler
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.uniqueCommandPath
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

private val logger = KotlinLogging.logger { }

class DefaultBucketKeySupplier internal constructor(private val scope: RateLimitScope) : BucketKeySupplier {

    private val handlers = ServiceLoader.load(RequestHandler::class.java) + RequestHandler { _, context ->
        when (context) {
            is TextCommandRateLimitingContext -> getKey(context.event, context.commandInfo)
            is ApplicationCommandRateLimitingContext -> getKey(context.event)
            is ComponentRateLimitingContext -> getKey(context.event, context.rateLimitReference)
            else -> null
        }
    }

    override fun getKey(context: RateLimitingContext): Key {
        for (handler in handlers) {
            val key = handler.handle(this, context)
            if (key != null) {
                return key
            }
        }

        throwInternal("Unsupported context: ${context.javaClass.name}")
    }

    private fun getKey(event: MessageReceivedEvent, commandInfo: TextCommandInfo): Key {
        if (!event.isFromGuild) throwInternal("Text commands can't run outside of a guild")
        val path = commandInfo.path
        return when (scope) {
            USER -> UserKey(path.fullPath, event.author.idLong)
            USER_PER_GUILD -> UserAtPlaceKey(path.fullPath, event.guild.idLong, event.author.idLong)
            USER_PER_CHANNEL -> UserAtPlaceKey(path.fullPath, event.channel.idLong, event.author.idLong)
            GUILD -> PlaceKey(path.fullPath, event.guild.idLong)
            CHANNEL -> PlaceKey(path.fullPath, event.channel.idLong)
        }
    }

    private fun getKey(event: GenericCommandInteractionEvent) =
        getRateLimitKey(event, event.uniqueCommandPath)

    private fun getKey(event: GenericComponentInteractionCreateEvent, rateLimitReference: ComponentRateLimitReference) =
        getRateLimitKey(event, rateLimitReference.toBucketKey())

    fun getRateLimitKey(event: Interaction, identifier: String): Key {
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

    fun interface RequestHandler {
        fun handle(instance: DefaultBucketKeySupplier, context: RateLimitingContext): Key?
    }
}

private object StringBucketKeyTransformer : BucketKeyTransformer<String> {
    override fun transform(key: Key): String {
        return when (key) {
            is PlaceKey -> "${key.identifier} ${key.id}"
            is UserKey -> "${key.identifier} ${key.id}"
            is UserAtPlaceKey -> "${key.identifier} ${key.placeId} ${key.userId}"
        }
    }
}

internal class DefaultProxyRateLimiter internal constructor(
    private val scope: RateLimitScope,
    proxyManager: ProxyManager<String>,
    bucketConfigurationSupplier: BucketConfigurationSupplier,
    private val deleteOnRefill: Boolean,
) : RateLimiter,
    BucketAccessor by ProxyBucketAccessor(proxyManager, DefaultBucketKeySupplier(scope), StringBucketKeyTransformer, bucketConfigurationSupplier),
    RateLimitHandler by DefaultRateLimitHandler(scope, deleteOnRefill) {

    override fun toString(): String {
        return "DefaultProxyRateLimiter(scope=$scope, deleteOnRefill=$deleteOnRefill)"
    }
}
