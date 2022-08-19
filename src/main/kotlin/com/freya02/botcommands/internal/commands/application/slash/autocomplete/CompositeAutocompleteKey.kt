package com.freya02.botcommands.internal.commands.application.slash.autocomplete

class CompositeAutocompleteKey(
    private val keys: Array<String>,
    private val guildId: Long,
    private val channelId: Long,
    private val userId: Long
) {
    private val length: Int
    private val hashCode: Int

    init {
        this.length = keys.sumOf { it.length }
        this.hashCode = let {
            var hashCode = keys.contentHashCode()
            hashCode = 31 * hashCode + guildId.hashCode()
            hashCode = 31 * hashCode + channelId.hashCode()
            hashCode = 31 * hashCode + userId.hashCode()

            return@let hashCode
        }
    }

    fun length(): Int {
        return length
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompositeAutocompleteKey

        if (!keys.contentEquals(other.keys)) return false
        if (guildId != other.guildId) return false
        if (channelId != other.channelId) return false
        if (userId != other.userId) return false

        return true
    }
}