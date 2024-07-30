package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.builder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteCacheInfoBuilder
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteCacheInfo

internal class AutocompleteCacheInfoBuilderImpl internal constructor() : AutocompleteCacheInfoBuilder {
    internal fun build() = AutocompleteCacheInfo(this)

    override var forceCache: Boolean = false
    override var cacheSize: Long = 2048

    override var compositeKeys: List<String> = emptyList()

    override var guildLocal: Boolean = false
    override var userLocal: Boolean = false
    override var channelLocal: Boolean = false
}