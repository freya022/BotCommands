package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.builder.CaffeineAutocompleteCacheFactoryBuilder

/**
 * Helper interface for Caffeine-backed autocomplete cache.
 *
 * @see CaffeineAutocompleteCache.builder
 */
interface CaffeineAutocompleteCache {

    companion object {

        /**
         * Creates a builder for a factory of [CaffeineAutocompleteCache], must be passed to [AutocompleteInfoBuilder.cache]
         */
        @JvmStatic
        fun builder(): CaffeineAutocompleteCacheFactoryBuilder = CaffeineAutocompleteCacheFactoryBuilder()
    }
}

/**
 * Sets up autocomplete caching backed by Caffeine.
 *
 * The [cache size][CaffeineAutocompleteCacheFactoryBuilder.cacheSize] is configurable.
 */
inline fun AutocompleteInfoBuilder.caffeineCache(block: CaffeineAutocompleteCacheFactoryBuilder.() -> Unit = {}) {
    cache(CaffeineAutocompleteCache.builder().apply(block).build())
}
