package com.freya02.botcommands.api.commands.application.slash.autocomplete.builder

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode

class AutocompleteCacheInfoBuilder internal constructor() {
    internal fun build() = AutocompleteCacheInfo(this)

    var cacheMode: AutocompleteCacheMode = AutocompleteCacheMode.NO_CACHE
    var cacheSize: Long = 2048
    var guildLocal: Boolean = false
    var userLocal: Boolean = false
    var channelLocal: Boolean = false
}