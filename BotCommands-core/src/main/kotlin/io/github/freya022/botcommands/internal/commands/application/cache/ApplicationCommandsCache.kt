package io.github.freya022.botcommands.internal.commands.application.cache

internal interface ApplicationCommandsCache {
    suspend fun tryRead(): ApplicationCommandsData

    suspend fun write(commandBytes: ByteArray, metadataBytes: ByteArray)
}
