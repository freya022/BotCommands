package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.internal.utils.reference
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isWritable
import kotlin.io.path.pathString

//TODO look into abstracting this into in-memory/in-file/in-database storage
// Perhaps the configuration would look like `commandCache = FileCommandCache(baseDir)`
// In-memory storage should not be configurable, as it is used as a fallback for missing write permissions
@Lazy // The service is requested when JDA is available
@BService
internal class ApplicationCommandsCache(jda: JDA, applicationConfig: BApplicationConfig) {
    private val cachePath: Path

    init {
        val dataDirectory = applicationConfig.commandCachePath ?: run {
            val appDataDirectory = when {
                "Windows" in System.getProperty("os.name") -> System.getenv("appdata")
                else -> "/var/tmp"
            }
            Path(appDataDirectory).resolve("BotCommands")
        }

        //TODO maybe only log and have this class serve as an in-memory cache
        check(dataDirectory.isWritable()) {
            // Don't use absolutePathString in case it also produces an exception
            "Cannot write to '${dataDirectory.pathString}', try setting a different path in ${BApplicationConfig::commandCachePath.reference}"
        }

        cachePath = dataDirectory
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