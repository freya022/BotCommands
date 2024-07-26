package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.freya022.botcommands.internal.application.diff.*
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

        fun isJsonContentSame(context: BContextImpl, oldContentBytes: ByteArray, newContentBytes: ByteArray): Boolean {
            val oldMap = DefaultObjectMapper.readList(oldContentBytes)
            val newMap = DefaultObjectMapper.readList(newContentBytes)

            val isSame: Boolean
            DiffLogger.getLogger(context).apply {
                isSame = checkDiff(oldMap, newMap)
                printLogs()
            }

            return isSame
        }

        context(DiffLogger)
        internal fun checkDiff(oldObj: Any?, newObj: Any?): Boolean {
            if (oldObj == null && newObj == null) {
                return logSame("Both null")
            }

            if (oldObj == null) {
                return logDifferent("oldObj is null")
            } else if (newObj == null) {
                return logDifferent("newObj is null")
            }

            if (oldObj.javaClass != newObj.javaClass) {
                return logDifferent("Class type not equal: %s to %s", oldObj.javaClass.simpleName, newObj.javaClass.simpleName)
            }

            return if (oldObj is Map<*, *> && newObj is Map<*, *>) {
                checkMap(oldObj, newObj)
            } else if (oldObj is List<*> && newObj is List<*>) {
                checkList(oldObj, newObj)
            } else {
                return when (oldObj == newObj) {
                    true -> logSame("Same object: %s to %s", oldObj, newObj)
                    false -> logDifferent("Not same object: %s to %s", oldObj, newObj)
                }
            }
        }

        context(DiffLogger)
        private fun checkList(oldList: List<*>, newList: List<*>): Boolean {
            if (oldList.size != newList.size)
                return logDifferent("List is not of the same size")

            oldList.indices.forEach { i ->
                withKey(i.toString()) {
                    // Try to find an object with the same content but at a different index
                    // Whether it is effectively different depends on the type of the object
                    val index = newList.indexOfFirst { ignoreLogs { checkDiff(oldList[i], it) } }
                    if (index == -1)
                        return logDifferent("List item not found: %s to %s", oldList[i], newList[i])

                    if (index != i) {
                        // Found the obj somewhere else, let's see if the object is an option
                        val oldObj = oldList[index]
                        if (oldObj is Map<*, *> && "autocomplete" in oldObj) {
                            // Is an option
                            return logDifferent("Final command option has changed place from index %s to %s : %s",
                                i,
                                index,
                                oldList[i]
                            )
                        } else {
                            // Is not an option
                            logSame("Found exact object at index %s (original object at %s) : %s", index, i, oldList[i])
                            return@forEach // Look at other options
                        }
                    } else {
                        // Same index
                        logSame("Found exact object at same index : %s", i, oldList[i])
                        return@forEach // Look at other options
                    }
                }
            }

            return true
        }

        context(DiffLogger)
        private fun checkMap(oldMap: Map<*, *>, newMap: Map<*, *>): Boolean {
            val missingKeys = oldMap.keys - newMap.keys
            if (missingKeys.isNotEmpty())
                return logDifferent("Missing keys: %s", missingKeys)

            val addedKeys = newMap.keys - oldMap.keys
            if (addedKeys.isNotEmpty())
                return logDifferent("Added keys: %s", addedKeys)

            for (key in oldMap.keys) {
                withKey(key.toString()) {
                    if (!checkDiff(oldMap[key], newMap[key])) {
                        return logDifferent("Map value not equal for key '%s': %s to %s", key, oldMap[key], newMap[key])
                    }
                }
            }

            return true
        }
    }
}