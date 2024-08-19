package io.github.freya022.botcommands.internal.commands.application.cache

import net.dv8tion.jda.api.entities.Guild

internal class MemoryApplicationCommandsCache internal constructor(
    private val guild: Guild?
) : ApplicationCommandsCache {

    private lateinit var commands: ByteArray
    private lateinit var metadata: ByteArray

    override fun hasCommands(): Boolean {
        return ::commands.isInitialized
    }

    override fun hasMetadata(): Boolean {
        return ::metadata.isInitialized
    }

    override fun readCommands(): String {
        return commands.decodeToString()
    }

    override fun readMetadata(): String {
        return metadata.decodeToString()
    }

    override fun writeCommands(bytes: ByteArray) {
        commands = bytes
    }

    override fun writeMetadata(bytes: ByteArray) {
        metadata = bytes
    }

    override fun toString(): String {
        return "MemoryApplicationCommandsCache(guild=${guild?.id})"
    }
}