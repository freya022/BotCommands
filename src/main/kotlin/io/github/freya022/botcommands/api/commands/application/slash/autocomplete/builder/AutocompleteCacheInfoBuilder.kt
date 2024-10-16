package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder

interface AutocompleteCacheInfoBuilder {
    /**
     * Whether the cache should be used even if [autocomplete cache is disabled][BConfig.disableAutocompleteCache].
     *
     * This could be useful if your autocomplete is heavy even in a development environment.
     *
     * @see BConfigBuilder.disableAutocompleteCache
     * @see CacheAutocomplete.forceCache
     */
    var forceCache: Boolean

    /**
     * @see CacheAutocomplete.cacheSize
     */
    var cacheSize: Long

    /**
     * @see CacheAutocomplete.compositeKeys
     */
    var compositeKeys: List<String>

    /**
     * @see CacheAutocomplete.userLocal
     */
    var guildLocal: Boolean

    /**
     * @see CacheAutocomplete.userLocal
     */
    var userLocal: Boolean

    /**
     * @see CacheAutocomplete.channelLocal
     */
    var channelLocal: Boolean
}
