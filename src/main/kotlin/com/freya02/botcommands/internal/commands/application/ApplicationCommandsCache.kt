package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.core.ServiceStart
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.diff.DiffLogger
import com.google.gson.Gson
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.utils.data.DataArray
import java.nio.file.Files
import java.nio.file.Path

@BService(ServiceStart.LAZY)
internal class ApplicationCommandsCache(jda: JDA) {
    private val cachePath = Path.of(System.getProperty("java.io.tmpdir"), "${jda.selfUser.id}slashcommands")
    val globalCommandsPath: Path = cachePath.resolve("globalCommands.json")

    init {
        Files.createDirectories(cachePath)
    }

    fun getGuildCommandsPath(guild: Guild): Path {
        return cachePath.resolve(guild.id).resolve("commands.json")
    }

    companion object {
        private val gson = Gson()

        fun Collection<CommandData>.toJsonBytes(): ByteArray = DataArray.empty().addAll(this).toJson()

        fun isJsonContentSame(context: BContextImpl, oldContentBytes: ByteArray, newContentBytes: ByteArray): Boolean {
            val oldMap = gson.fromJson(oldContentBytes.decodeToString(), Any::class.java)
            val newMap = gson.fromJson(newContentBytes.decodeToString(), Any::class.java)

            val isSame = DiffLogger.getLogger(context).let { diffLogger ->
                checkDiff(oldMap, newMap, diffLogger, 0).also {
                    diffLogger.printLogs()
                }
            }

            return isSame
        }

        private fun checkDiff(oldObj: Any?, newObj: Any?, logger: DiffLogger, indent: Int): Boolean {
            if (oldObj == null && newObj == null) {
                return true
            }

            if (oldObj == null) {
                logger.trace(indent, "oldObj is null")
                return false
            } else if (newObj == null) {
                logger.trace(indent, "newObj is null")
                return false
            }

            if (oldObj.javaClass != newObj.javaClass) {
                logger.trace(indent, "Class type not equal: %s to %s", oldObj.javaClass.simpleName, newObj.javaClass.simpleName)
                return false
            }

            if (oldObj is Map<*, *> && newObj is Map<*, *>) {
                if (!checkMap(oldObj, newObj, logger, indent)) return false
            } else if (oldObj is List<*> && newObj is List<*>) {
                if (!checkList(oldObj, newObj, logger, indent)) return false
            } else {
                return (oldObj == newObj).also { equals ->
                    if (!equals) logger.trace(indent, "Not same object: %s to %s", oldObj, newObj)
                }
            }

            return true
        }

        private fun checkList(oldList: List<*>, newList: List<*>, logger: DiffLogger, indent: Int): Boolean {
            if (oldList.size != newList.size) return false

            for (i in oldList.indices) {
                var found = false
                var index = -1
                for (o in newList) {
                    index++
                    if (checkDiff(oldList[i], o, logger, indent + 1)) {
                        found = true
                        break
                    }
                }

                if (found) {
                    //If command options (parameters, not subcommands, not groups) are moved
                    // then it means the command data changed
                    if (i != index) {
                        //Check if any final command property is here,
                        // such as autocomplete, or required
                        if (oldList[index] is Map<*, *>) {
                            val map = oldList[index] as Map<*, *>
                            if (map["autocomplete"] != null) {
                                //We found a real command option that has **changed index**,
                                // this is NOT equal under different indexes
                                logger.trace(
                                    indent,
                                    "Final command option has changed place from index %s to %s : %s",
                                    i,
                                    index,
                                    oldList[i]
                                )
                                return false
                            }
                        }
                    }

                    logger.trace(indent, "Found exact object at index %s (original object at %s) : %s", index, i, oldList[i])
                    continue
                }

                if (!checkDiff(oldList[i], newList[i], logger, indent + 1)) {
                    logger.trace(indent, "List item not equal: %s to %s", oldList[i], newList[i])
                    return false
                }
            }

            return true
        }

        private fun checkMap(oldMap: Map<*, *>, newMap: Map<*, *>, logger: DiffLogger, indent: Int): Boolean {
            if (!oldMap.keys.containsAll(newMap.keys)) return false

            for (key in oldMap.keys) {
                if (!checkDiff(oldMap[key], newMap[key], logger, indent + 1)) {
                    logger.trace(indent, "Map value not equal for key '%s': %s to %s", key, oldMap[key], newMap[key])
                    return false
                }
            }

            return true
        }
    }
}