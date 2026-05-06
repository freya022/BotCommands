package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations

/**
 * Configures how the autocomplete cache key will be formed.
 * If any component of the key changes, a new set of choices will be generated.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutocompleteCacheKey(
    /**
     * The set of **option names** (the one you see on Discord) which forms the cache key.
     * The option on which this autocomplete is applied on, will always be included in the key.
     *
     * This could be useful when making autocompleting based on multiple option values.
     */
    val compositeKeys: Array<String> = [],

    /**
     * If the cache key includes the guild, meaning the cached values are specific to that guild,
     * if a query is from a different guild, new values are generated.
     *
     * If there are multiple entity types, for example guild and user,
     * new values are computed if either the guild or the user is different.
     */
    val guildLocal: Boolean = false,

    /**
     * If the cache key includes the user, meaning the cached values are specific to that user,
     * if a query is from a different user, new values are generated.
     *
     * If there are multiple entity types, for example guild and user,
     * new values are computed if either the guild or the user is different.
     */
    val userLocal: Boolean = false,

    /**
     * If the cache key includes the channel, meaning the cached values are specific to that channel,
     * if a query is from a different channel, new values are generated.
     *
     * If there are multiple entity types, for example channel and user,
     * new values are computed if either the channel or the user is different.
     */
    val channelLocal: Boolean = false
)
