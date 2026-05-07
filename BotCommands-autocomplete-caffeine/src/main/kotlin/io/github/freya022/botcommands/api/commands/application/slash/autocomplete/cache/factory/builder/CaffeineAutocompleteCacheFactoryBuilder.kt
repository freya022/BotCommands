package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.builder

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.CaffeineAutocompleteCache
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.AutocompleteCacheFactory
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.factory.CaffeineAutocompleteCacheFactoryImpl

/**
 * Caffeine-backed autocomplete cache factory builder.
 * Use [CaffeineAutocompleteCache.builder] to create instances of this.
 *
 * The cache key can be configured with [compositeKeys] and the `*Local` switches.
 */
class CaffeineAutocompleteCacheFactoryBuilder internal constructor() : AutocompleteCacheFactoryBuilder {
    override var forceCache: Boolean = false

    override var compositeKeys: List<String> = emptyList()

    override var guildLocal: Boolean = false
    override var channelLocal: Boolean = false
    override var userLocal: Boolean = false

    /**
     * Sets the caffeine cache size **in kilobytes (KB)**.
     *
     * The current size is calculated by the length of the cache key, and the sum of the choice names and values.
     *
     * @see Caffeine.maximumWeight
     */
    var cacheSize: Long = 2048
        set(value) {
            require(value >= 0) { "Size must be greater than or equal to 0" }
            field = value
        }

    /**
     * Sets the caffeine cache size **in kilobytes (KB)**.
     *
     * The current size is calculated by the length of the cache key, and the sum of the choice names and values.
     *
     * @see Caffeine.maximumWeight
     */
    fun cacheSize(size: Long): CaffeineAutocompleteCacheFactoryBuilder {
        this.cacheSize = size
        return this
    }

    /**
     * Builds the factory.
     */
    fun build(): AutocompleteCacheFactory {
        return CaffeineAutocompleteCacheFactoryImpl(forceCache, LinkedHashSet(compositeKeys).unmodifiableView(), guildLocal, channelLocal, userLocal, cacheSize)
    }
}
