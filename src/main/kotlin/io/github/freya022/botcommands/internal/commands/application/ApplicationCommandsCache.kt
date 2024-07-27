package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.freya022.botcommands.internal.application.diff.DiffLogger
import io.github.freya022.botcommands.internal.commands.application.diff.NewApplicationCommandDiffEngine
import io.github.freya022.botcommands.internal.core.BContextImpl
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.utils.data.DataArray
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

    val globalCommandsPath: Path = cachePath.resolve("globalCommands.json")
    val globalCommandsMetadataPath: Path = cachePath.resolve("globalCommands_metadata.json")

    fun getGuildCommandsPath(guild: Guild): Path {
        return cachePath.resolve(guild.id).resolve("commands.json")
    }

    fun getGuildCommandsMetadataPath(guild: Guild): Path {
        return cachePath.resolve(guild.id).resolve("commands_metadata.json")
    }

    companion object {
        fun Collection<CommandData>.toJsonBytes(): ByteArray = DataArray.empty().addAll(this).toJson()

        @Suppress("UNCHECKED_CAST")
        fun isJsonContentSame(context: BContextImpl, oldContentBytes: ByteArray, newContentBytes: ByteArray): Boolean {
            val oldCommands = DefaultObjectMapper.readList(oldContentBytes) as List<Map<String, *>>
            val newCommands = DefaultObjectMapper.readList(newContentBytes) as List<Map<String, *>>

            val isSame = DiffLogger.withLogger(context) {
                NewApplicationCommandDiffEngine.checkCommands(oldCommands, newCommands)
            }

            return isSame
        }
    }
}