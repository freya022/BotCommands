package io.github.freya022.botcommands.internal.commands.application.cache

import net.dv8tion.jda.api.entities.Guild

internal class MemoryApplicationCommandsCache internal constructor(
    private val guild: Guild?
) : ApplicationCommandsCache {

    private lateinit var commands: ByteArray
    private lateinit var metadata: ByteArray

    override suspend fun tryRead(): ApplicationCommandsData {
        return ApplicationCommandsData(
            if (::commands.isInitialized) commands.decodeToString() else null,
            if (::metadata.isInitialized) metadata.decodeToString() else null,
        )
    }

    override suspend fun write(commandBytes: ByteArray, metadataBytes: ByteArray) {
        commands = commandBytes
        metadata = metadataBytes
    }

    override fun toString(): String {
        return "MemoryApplicationCommandsCache(guild=${guild?.id})"
    }
}