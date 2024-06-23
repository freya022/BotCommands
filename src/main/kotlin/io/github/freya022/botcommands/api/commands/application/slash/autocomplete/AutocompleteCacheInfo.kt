package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteCacheInfoBuilder
import io.github.freya022.botcommands.internal.utils.toDiscordString

class AutocompleteCacheInfo internal constructor(builder: AutocompleteCacheInfoBuilder) {
    val force: Boolean = builder.forceCache
    val cacheMode: AutocompleteCacheMode = AutocompleteCacheMode.CONSTANT_BY_KEY
    val cacheSize: Long = builder.cacheSize
    val compositeKeys: List<String> = builder.compositeKeys.map { it.toDiscordString() }
    val guildLocal: Boolean = builder.guildLocal
    val userLocal: Boolean = builder.userLocal
    val channelLocal: Boolean = builder.channelLocal

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutocompleteCacheInfo

        if (force != other.force) return false
        if (cacheMode != other.cacheMode) return false
        if (cacheSize != other.cacheSize) return false
        if (compositeKeys != other.compositeKeys) return false
        if (guildLocal != other.guildLocal) return false
        if (userLocal != other.userLocal) return false
        if (channelLocal != other.channelLocal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = force.hashCode()
        result = 31 * result + cacheMode.hashCode()
        result = 31 * result + cacheSize.hashCode()
        result = 31 * result + compositeKeys.hashCode()
        result = 31 * result + guildLocal.hashCode()
        result = 31 * result + userLocal.hashCode()
        result = 31 * result + channelLocal.hashCode()
        return result
    }
}