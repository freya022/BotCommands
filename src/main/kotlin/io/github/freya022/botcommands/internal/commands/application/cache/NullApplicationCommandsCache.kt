package io.github.freya022.botcommands.internal.commands.application.cache

internal data object NullApplicationCommandsCache : ApplicationCommandsCache {
    override suspend fun tryRead(): ApplicationCommandsData {
        return ApplicationCommandsData(null, null)
    }

    override suspend fun write(commandBytes: ByteArray, metadataBytes: ByteArray) {}
}