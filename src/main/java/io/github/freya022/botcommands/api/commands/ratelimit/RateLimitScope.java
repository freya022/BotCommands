package io.github.freya022.botcommands.api.commands.ratelimit;

/**
 * Scope on which a rate limit applies.
 *
 * @see RateLimiter
 */
public enum RateLimitScope {
    /** Limits the usage rate by the users, regardless of the execution guild/channel. */
    USER(false),
    /** Limits the usage rate by the users, per guild (i.e., the rate limit is local to the guild). */
    USER_PER_GUILD(true),
    /** Limits the usage rate by the users, per channel (i.e., the rate limit is local to the channel). */
    USER_PER_CHANNEL(false),
    /** Limits the usage rate by the guild. */
    GUILD(true),
    /** Limits the usage rate by the channel. */
    CHANNEL(false);

    private final boolean isGuild;

    RateLimitScope(boolean isGuild) {
        this.isGuild = isGuild;
    }

    public boolean isGuild() {
        return isGuild;
    }
}
