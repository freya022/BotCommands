package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations

import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfig

/**
 * Enables autocomplete caching.
 *
 * This will cache results by key, which is the input of the focused option.<br>
 * However, you can use composite keys if you want to cache based off multiple option values,
 * see [compositeKeys] for more details.
 *
 * @see SlashOption @SlashOption
 * @see AutocompleteHandler @AutocompleteHandler
 *
 * @see AutocompleteInfoBuilder.cache DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheAutocomplete(
    /**
     * Whether the cache should be used even if [autocomplete cache is disabled][BApplicationConfig.disableAutocompleteCache].
     *
     * This could be useful if your autocomplete is heavy even in a development environment.
     */
    val forceCache: Boolean = false,

    /**
     * Sets the cache size for this autocomplete cache, **in kilobytes (KB)**.
     */
    val cacheSize: Long = 2048,

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
