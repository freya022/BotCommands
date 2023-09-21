package com.freya02.botcommands.api.commands;

import com.freya02.botcommands.api.commands.ratelimit.DefaultRateLimitHelper;

/**
 * Provides rate limit scopes for the {@link DefaultRateLimitHelper default rate limit implementation}.
 */
public enum RateLimitScope {
    /** Limits the usage rate by the users, regardless of the execution guild/channel. */
    USER,
    /** Limits the usage rate by the users, per guild (i.e., the rate limit is local to the guild). */
    USER_PER_GUILD,
    /** Limits the usage rate by the users, per channel (i.e., the rate limit is local to the channel). */
    USER_PER_CHANNEL,
    /** Limits the usage rate by the guild. */
    GUILD,
    /** Limits the usage rate by the channel. */
    CHANNEL
}
