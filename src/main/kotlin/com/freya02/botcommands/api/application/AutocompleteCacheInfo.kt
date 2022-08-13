package com.freya02.botcommands.api.application

import com.freya02.botcommands.api.application.builder.AutocompleteCacheInfoBuilder
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteCacheMode

class AutocompleteCacheInfo internal constructor(val builder: AutocompleteCacheInfoBuilder) {
    val cacheMode: AutocompleteCacheMode = builder.cacheMode
    val cacheSize: Long = builder.cacheSize
    val guildLocal: Boolean = builder.guildLocal
    val userLocal: Boolean = builder.userLocal
    val channelLocal: Boolean = builder.channelLocal
}