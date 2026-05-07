package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.builder

import io.github.freya022.botcommands.api.core.config.BApplicationConfig

/**
 * Base builder for autocomplete cache factories.
 *
 * This should not be implemented, look for subtypes instead.
 */
interface AutocompleteCacheFactoryBuilder {
    /**
     * Whether the cache should be used even if [autocomplete cache is disabled][BApplicationConfig.disableAutocompleteCache].
     *
     * This could be useful if your autocomplete is heavy even in a development environment.
     */
    var forceCache: Boolean

    /**
     * Whether the cache should be used even if [autocomplete cache is disabled][BApplicationConfig.disableAutocompleteCache].
     *
     * This could be useful if your autocomplete is heavy even in a development environment.
     */
    fun forceCache(): AutocompleteCacheFactoryBuilder {
        forceCache = true
        return this
    }

    /**
     * The set of **option names** (the one you see on Discord) which forms the cache key.
     * The option on which this autocomplete is applied on, will always be included in the key.
     *
     * This could be useful when making autocompleting based on multiple option values.
     */
    var compositeKeys: List<String>

    /**
     * The set of **option names** (the one you see on Discord) which forms the cache key.
     * The option on which this autocomplete is applied on, will always be included in the key.
     *
     * This could be useful when making autocompleting based on multiple option values.
     */
    fun compositeKeys(vararg keys: String): AutocompleteCacheFactoryBuilder {
        compositeKeys = keys.toList()
        return this
    }

    /**
     * If the cache key includes the guild, meaning the cached values are specific to that guild,
     * if a query is from a different guild, new values are generated.
     *
     * If there are multiple entity types, for example guild and user,
     * new values are computed if either the guild or the user is different.
     */
    var guildLocal: Boolean

    /**
     * If the cache key includes the guild, meaning the cached values are specific to that guild,
     * if a query is from a different guild, new values are generated.
     *
     * If there are multiple entity types, for example guild and user,
     * new values are computed if either the guild or the user is different.
     */
    fun guildLocal(): AutocompleteCacheFactoryBuilder {
        guildLocal = true
        return this
    }

    /**
     * If the cache key includes the user, meaning the cached values are specific to that user,
     * if a query is from a different user, new values are generated.
     *
     * If there are multiple entity types, for example guild and user,
     * new values are computed if either the guild or the user is different.
     */
    var userLocal: Boolean

    /**
     * If the cache key includes the user, meaning the cached values are specific to that user,
     * if a query is from a different user, new values are generated.
     *
     * If there are multiple entity types, for example guild and user,
     * new values are computed if either the guild or the user is different.
     */
    fun userLocal(): AutocompleteCacheFactoryBuilder {
        userLocal = true
        return this
    }

    /**
     * If the cache key includes the channel, meaning the cached values are specific to that channel,
     * if a query is from a different channel, new values are generated.
     *
     * If there are multiple entity types, for example channel and user,
     * new values are computed if either the channel or the user is different.
     */
    var channelLocal: Boolean

    /**
     * If the cache key includes the channel, meaning the cached values are specific to that channel,
     * if a query is from a different channel, new values are generated.
     *
     * If there are multiple entity types, for example channel and user,
     * new values are computed if either the channel or the user is different.
     */
    fun channelLocal(): AutocompleteCacheFactoryBuilder {
        channelLocal = true
        return this
    }
}
