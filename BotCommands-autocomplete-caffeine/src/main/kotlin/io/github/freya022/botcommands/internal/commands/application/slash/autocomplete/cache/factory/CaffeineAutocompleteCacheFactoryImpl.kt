package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.factory

import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.AbstractAutocompleteCache
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.CaffeineAutocompleteCacheImpl

internal class CaffeineAutocompleteCacheFactoryImpl internal constructor(
    override val force: Boolean,
    private val compositeKeys: Set<String>,
    private val guildLocal: Boolean,
    private val channelLocal: Boolean,
    private val userLocal: Boolean,
    private val cacheSize: Long,
) : AbstractAutocompleteCacheFactory() {

    override fun create(): AbstractAutocompleteCache {
        return CaffeineAutocompleteCacheImpl(compositeKeys, guildLocal, channelLocal, userLocal, cacheSize)
    }
}
