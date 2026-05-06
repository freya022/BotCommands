package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations

import com.github.benmanes.caffeine.cache.Caffeine

/**
 * Enables Caffeine-backed autocomplete caching.
 *
 * The cache key can be configured with [AutocompleteCacheKey].
 *
 * @see ForceAutocompleteCache
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CaffeineAutocompleteCache(
    /**
     * Sets the caffeine cache size **in kilobytes (KB)**.
     *
     * The current size is calculated by the length of the cache key, and the sum of the choice names and values.
     *
     * @see Caffeine.maximumWeight
     */
    val cacheSize: Long = 2048,
)
