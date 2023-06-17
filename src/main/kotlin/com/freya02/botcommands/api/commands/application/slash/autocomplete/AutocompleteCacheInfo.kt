package com.freya02.botcommands.api.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteCacheInfoBuilder

class AutocompleteCacheInfo internal constructor(builder: AutocompleteCacheInfoBuilder) {
    val cacheMode: AutocompleteCacheMode = builder.cacheMode
    val cacheSize: Long = builder.cacheSize
    val compositeKeys: List<String> = builder.compositeKeys
    val guildLocal: Boolean = builder.guildLocal
    val userLocal: Boolean = builder.userLocal
    val channelLocal: Boolean = builder.channelLocal

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutocompleteCacheInfo

        if (cacheMode != other.cacheMode) return false
        if (cacheSize != other.cacheSize) return false
        if (compositeKeys != other.compositeKeys) return false
        if (guildLocal != other.guildLocal) return false
        if (userLocal != other.userLocal) return false
        if (channelLocal != other.channelLocal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cacheMode.hashCode()
        result = 31 * result + cacheSize.hashCode()
        result = 31 * result + compositeKeys.hashCode()
        result = 31 * result + guildLocal.hashCode()
        result = 31 * result + userLocal.hashCode()
        result = 31 * result + channelLocal.hashCode()
        return result
    }
}