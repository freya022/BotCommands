package io.github.freya022.botcommands.internal.commands.application.cache

internal interface ApplicationCommandsCache {
    fun hasCommands(): Boolean
    fun hasMetadata(): Boolean

    fun readCommands(): String
    fun readMetadata(): String

    //TODO not sure if we should accept the data rather than the bytes
    fun writeCommands(bytes: ByteArray)
    fun writeMetadata(bytes: ByteArray)
}
