package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * [BucketAccessor] implementation with a [ProxyManager] to retrieve buckets,
 * and a key made using [keySupplier].
 *
 * @param proxyManager          Scope of the rate limit, see [RateLimitScope] values.
 * @param keySupplier           Supplies the key to create/retrieve a bucket using the [proxyManager]
 * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
 */
class ProxyBucketAccessor<K>(
    private val proxyManager: ProxyManager<K>,
    private val keySupplier: BucketKeySupplier<K>,
    private val configurationSupplier: BucketConfigurationSupplier,
) : BucketAccessor {

    override suspend fun getBucket(
        context: BContext,
        event: MessageReceivedEvent,
        commandInfo: TextCommandInfo
    ): Bucket {
        val key = keySupplier.getKey(context, event, commandInfo)
        return proxyManager.getProxy(key) { configurationSupplier.getConfiguration(context, event, commandInfo) }
    }

    override suspend fun getBucket(
        context: BContext,
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Bucket {
        val key = keySupplier.getKey(context, event, commandInfo)
        return proxyManager.getProxy(key) { configurationSupplier.getConfiguration(context, event, commandInfo) }
    }

    override suspend fun getBucket(context: BContext, event: GenericComponentInteractionCreateEvent, rateLimitReference: ComponentRateLimitReference): Bucket {
        val key = keySupplier.getKey(context, event, rateLimitReference)
        return proxyManager.getProxy(key) { configurationSupplier.getConfiguration(context, event) }
    }
}