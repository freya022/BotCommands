package com.freya02.botcommands.api.commands.application.slash.autocomplete.builder

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.internal.commands.CommandDSL

@CommandDSL
class AutocompleteCacheInfoBuilder internal constructor(val cacheMode: AutocompleteCacheMode) {
    internal fun build() = AutocompleteCacheInfo(this)

    var cacheSize: Long = 2048
    var guildLocal: Boolean = false
    var userLocal: Boolean = false
    var channelLocal: Boolean = false
}
