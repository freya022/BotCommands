package com.freya02.botcommands.api.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteCacheInfoBuilder

class AutocompleteCacheInfo internal constructor(val builder: AutocompleteCacheInfoBuilder) {
    val cacheMode: AutocompleteCacheMode = builder.cacheMode
    val cacheSize: Long = builder.cacheSize
    val guildLocal: Boolean = builder.guildLocal
    val userLocal: Boolean = builder.userLocal
    val channelLocal: Boolean = builder.channelLocal
}