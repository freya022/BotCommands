package io.github.freya022.botcommands.internal.commands.application.cache

import io.github.freya022.botcommands.api.core.utils.overwriteBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import java.nio.file.Path
import kotlin.io.path.createDirectories
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

    override suspend fun tryRead(): ApplicationCommandsData = withContext(Dispatchers.IO) {
        ApplicationCommandsData(
            if (commandsPath.exists()) commandsPath.readText() else null,
            if (commandsMetadataPath.exists()) commandsMetadataPath.readText() else null,
        )
    }

    override suspend fun write(commandBytes: ByteArray, metadataBytes: ByteArray): Unit = withContext(Dispatchers.IO) {
        commandsPath.parent.createDirectories()

        commandsPath.overwriteBytes(commandBytes)
        commandsMetadataPath.overwriteBytes(metadataBytes)
    }

    override fun toString(): String {
        return "FileApplicationCommandsCache(cachePath='$cachePath', guild=${guild?.id})"
    }
}