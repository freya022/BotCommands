package com.freya02.botcommands.api.commands.application.slash.autocomplete.builder

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.config.BConfigBuilder
import com.freya02.botcommands.internal.commands.CommandDSL

@CommandDSL
class AutocompleteCacheInfoBuilder internal constructor(val cacheMode: AutocompleteCacheMode) {
    internal fun build() = AutocompleteCacheInfo(this)

    /**
     * Whether the cache should be used even if [autocomplete cache is disabled][BConfig.disableAutocompleteCache].
     *
     * This could be useful if your autocomplete is heavy even in a development environment.
     *
     * @see BConfigBuilder.disableAutocompleteCache
     */
    var forceCache: Boolean = false
    var cacheSize: Long = 2048
    var compositeKeys: List<String> = emptyList()
    var guildLocal: Boolean = false
    var userLocal: Boolean = false
    var channelLocal: Boolean = false
}
