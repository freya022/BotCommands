package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope.*
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.ProxyBucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import io.github.freya022.botcommands.api.commands.ratelimit.handler.RateLimitHandler
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.uniqueCommandPath
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction

private val logger = KotlinLogging.logger { }

private class DefaultBucketKeySupplier(private val scope: RateLimitScope) : BucketKeySupplier<String> {
    override fun getKey(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo): String {
        if (!event.isFromGuild) throwInternal("Text commands can't run outside of a guild")
        return commandInfo.path.fullPath + when (scope) {
            USER -> event.author.id
            USER_PER_GUILD -> "${event.guild.id} ${event.author.id}"
            USER_PER_CHANNEL -> "${event.channel.id} ${event.author.id}"
            GUILD -> event.guild.id
            CHANNEL -> event.channel.id
        }
    }

    override fun getKey(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo) =
        getRateLimitKey(event, event.uniqueCommandPath)

    override fun getKey(context: BContext, event: GenericComponentInteractionCreateEvent, rateLimitReference: ComponentRateLimitReference) =
        getRateLimitKey(event, rateLimitReference.toBucketKey())

    private fun getRateLimitKey(event: Interaction, identifier: String): String {
        if (scope.isGuild && !event.isFromGuild) {
            logger.warn { "Cannot get a bucket with the $scope scope outside of a guild, using the user ID instead." }
            return "$identifier ${event.user.id}"
        }

        return "$identifier " + when (scope) {
            USER -> event.user.id
            USER_PER_GUILD -> {
                val guild = event.guild ?: throwInternal("Guild should be present")
                "${guild.id} ${event.user.id}"
            }
            USER_PER_CHANNEL -> "${event.channelId} ${event.user.id}"
            GUILD -> {
                val guild = event.guild ?: throwInternal("Guild should be present")
                guild.id
            }
            CHANNEL -> event.channelId
        }
    }
}

internal class DefaultProxyRateLimiter internal constructor(
    private val scope: RateLimitScope,
    proxyManager: ProxyManager<String>,
    bucketConfigurationSupplier: BucketConfigurationSupplier,
    private val deleteOnRefill: Boolean,
) : RateLimiter,
    BucketAccessor by ProxyBucketAccessor(proxyManager, DefaultBucketKeySupplier(scope), bucketConfigurationSupplier),
    RateLimitHandler by DefaultRateLimitHandler(scope, deleteOnRefill) {

    override fun toString(): String {
        return "DefaultProxyRateLimiter(scope=$scope, deleteOnRefill=$deleteOnRefill)"
    }
}