package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.entities.UserSnowflake

/**
 * Holds owners of this bot.
 *
 * Bot owners are allowed to bypass cooldowns, user permission checks,
 * and have hidden commands shown.
 */
@InterfacedService(acceptMultiple = false)
interface BotOwners {
    /**
     * The owners of this bot, uses [BConfigBuilder.ownerIds] if configured,
     * else retrieve the owners from Discord, where only owners, admin and developers are taken.
     *
     * Allows bypassing cooldowns, user permission checks,
     * and having hidden commands shown.
     */
    val ownerIds: Collection<Long>

    /**
     * Whether this user is one of the bot owners.
     *
     * Allows bypassing cooldowns, user permission checks,
     * and having hidden commands shown.
     *
     * @see UserSnowflake.fromId
     */
    fun isOwner(user: UserSnowflake): Boolean

    operator fun contains(user: UserSnowflake): Boolean = isOwner(user)
}