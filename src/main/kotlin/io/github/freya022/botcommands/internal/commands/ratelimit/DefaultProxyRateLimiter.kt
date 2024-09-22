package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.ProxyBucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import io.github.freya022.botcommands.api.commands.ratelimit.handler.RateLimitHandler
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.math.BigDecimal

private val logger = KotlinLogging.logger { }

private class DefaultBucketKeySupplier(private val scope: RateLimitScope) : BucketKeySupplier<BigDecimal> {
    override fun getKey(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo) = event.toRateLimitKey()

    override fun getKey(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo) = event.toRateLimitKey()

    override fun getKey(context: BContext, event: GenericComponentInteractionCreateEvent) = event.toRateLimitKey()

    private fun MessageReceivedEvent.toRateLimitKey(): BigDecimal {
        if (!isFromGuild) throwInternal("Invalid rate limit scope for text commands")
        return when (scope) {
            RateLimitScope.USER -> BigDecimal(author.idLong)
            RateLimitScope.USER_PER_GUILD -> BigDecimal("${guild.id}${author.id}")
            RateLimitScope.USER_PER_CHANNEL -> BigDecimal("${channel.id}${author.id}")
            RateLimitScope.GUILD -> BigDecimal(guild.idLong)
            RateLimitScope.CHANNEL -> BigDecimal(channel.idLong)
        }
    }

    private fun GenericInteractionCreateEvent.toRateLimitKey(): BigDecimal {
        return when (scope) {
            RateLimitScope.USER -> BigDecimal(user.idLong)
            RateLimitScope.USER_PER_GUILD -> {
                val guild = guild ?: return fallbackUserKey(user)
                BigDecimal("${guild.id}${user.id}")
            }
            RateLimitScope.USER_PER_CHANNEL -> {
                if (isFromGuild) BigDecimal("${guildChannel.id}${user.id}") else fallbackUserKey(user)
            }
            RateLimitScope.GUILD -> {
                val guild = guild ?: return fallbackUserKey(user)
                BigDecimal(guild.idLong)
            }
            RateLimitScope.CHANNEL -> {
                if (isFromGuild) BigDecimal(guildChannel.idLong) else fallbackUserKey(user)
            }
        }
    }

    private fun fallbackUserKey(user: UserSnowflake): BigDecimal {
        logger.warn {
            "Tried to get an invalid rate limit bucket, rate limiters outside of guilds must only use the ${RateLimitScope.USER} scope. " +
                    "Returning an user bucket instead."
        }
        return BigDecimal(user.idLong)
    }
}

internal class DefaultProxyRateLimiter internal constructor(
    private val scope: RateLimitScope,
    proxyManager: ProxyManager<BigDecimal>,
    bucketConfigurationSupplier: BucketConfigurationSupplier,
    private val deleteOnRefill: Boolean,
) : RateLimiter,
    BucketAccessor by ProxyBucketAccessor(proxyManager, DefaultBucketKeySupplier(scope), bucketConfigurationSupplier),
    RateLimitHandler by DefaultRateLimitHandler(scope, deleteOnRefill) {

    override fun toString(): String {
        return "DefaultProxyRateLimiter(scope=$scope, deleteOnRefill=$deleteOnRefill)"
    }
}