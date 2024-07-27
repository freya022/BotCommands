package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Lazy // The service is requested when JDA is available
@BService
internal class ApplicationCommandsCache(jda: JDA) {
    private val cachePath: Path

    init {
        val appDataDirectory = when {
            "Windows" in System.getProperty("os.name") -> System.getenv("appdata")
            else -> "/var/tmp"
        }
        cachePath = Path(appDataDirectory)
            .resolve("BotCommands")
            .resolve("ApplicationCommands-${jda.selfUser.id}")
            .createDirectories()
    }

    internal val globalCommandsPath: Path = cachePath.resolve("globalCommands.json")
    internal val globalCommandsMetadataPath: Path = cachePath.resolve("globalCommands_metadata.json")

    internal fun getGuildCommandsPath(guild: Guild): Path {
        return cachePath.resolve(guild.id).resolve("commands.json")
    }

    internal fun getGuildCommandsMetadataPath(guild: Guild): Path {
        return cachePath.resolve(guild.id).resolve("commands_metadata.json")
    }
}