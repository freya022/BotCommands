package io.github.freya022.botcommands.internal.commands.application.cache

import io.github.freya022.botcommands.api.core.utils.overwriteBytes
import net.dv8tion.jda.api.entities.Guild
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

internal class FileApplicationCommandsCache internal constructor(
    private val cachePath: Path,
    private val guild: Guild?
) : ApplicationCommandsCache {

    private val commandsPath: Path
    private val commandsMetadataPath: Path

    init {
        if (guild != null) {
            val guildPath = cachePath.resolve(guild.id)
            commandsPath = guildPath.resolve("commands.json")
            commandsMetadataPath = guildPath.resolve("commands_metadata.json")
        } else {
            commandsPath = cachePath.resolve("globalCommands.json")
            commandsMetadataPath = cachePath.resolve("globalCommands_metadata.json")
        }
    }

    override fun hasCommands(): Boolean {
        return commandsPath.exists()
    }

    override fun hasMetadata(): Boolean {
        return commandsMetadataPath.exists()
    }

    override fun readCommands(): String {
        return commandsPath.readText()
    }

    override fun readMetadata(): String {
        return commandsMetadataPath.readText()
    }

    override fun writeCommands(bytes: ByteArray) {
        commandsPath.overwriteBytes(bytes)
    }

    override fun writeMetadata(bytes: ByteArray) {
        commandsMetadataPath.overwriteBytes(bytes)
    }

    override fun toString(): String {
        return "FileApplicationCommandsCache(cachePath='$cachePath', guild=${guild?.id})"
    }
}