package com.freya02.botcommands.commands.internal.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.application.IApplicationCommandManager
import com.freya02.botcommands.api.builder.DebugBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.application.ApplicationCommandDataMap
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.ApplicationCommandsCache
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.application.localization.BCLocalizationFunction
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.application.slash.SlashUtils2.getMethodOptions
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.*
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.nio.file.Files
import java.util.function.Function

private val LOGGER = Logging.getLogger()

internal class ApplicationCommandsUpdaterKt private constructor(
    private val context: BContextImpl,
    private val guild: Guild?,
    manager: IApplicationCommandManager
) {
    private val commandsCache = context.getService(ApplicationCommandsCacheKt::class)
    private val onlineCheck = context.isOnlineAppCommandCheckEnabled

    private val commandsCachePath = when (guild) {
        null -> commandsCache.globalCommandsPath
        else -> commandsCache.getGuildCommandsPath(guild)
    }

    private val subcommandGroupDataMap: MutableMap<String, SubcommandGroupData> = hashMapOf()
    val guildApplicationCommands: List<ApplicationCommandInfo>
    private val allCommandData: Collection<CommandData>

    init {
        Files.createDirectories(commandsCachePath.parent)

        guildApplicationCommands = manager.guildApplicationCommands

        allCommandData = computeCommands().allCommandData

        //Apply localization
        val localizationFunction: LocalizationFunction = BCLocalizationFunction(context)
        for (commandData in allCommandData) {
            commandData.setLocalizationFunction(localizationFunction)
        }
    }

    suspend fun shouldUpdateCommands(): Boolean {
        val oldBytes = when {
            onlineCheck -> {
                (guild?.retrieveCommands(true) ?: context.jda.retrieveCommands(true))
                    .await()
                    .map { CommandData.fromCommand(it) }
                    .let { ApplicationCommandsCacheKt.getCommandsBytes(it) }
            }
            else -> {
                if (Files.notExists(commandsCachePath)) {
                    LOGGER.trace("Updating commands because cache file does not exists")
                    return true
                }

                withContext(Dispatchers.IO) {
                    Files.readAllBytes(commandsCachePath)
                }
            }
        }

        val newBytes = ApplicationCommandsCacheKt.getCommandsBytes(allCommandData)
        return (!ApplicationCommandsCache.isJsonContentSame(oldBytes, newBytes)).also { needUpdate ->
            if (needUpdate) {
                LOGGER.trace("Updating commands because content is not equal")

                if (DebugBuilder.isLogApplicationDiffsEnabled()) {
                    LOGGER.trace("Old commands bytes: {}", oldBytes.decodeToString())
                    LOGGER.trace("New commands bytes: {}", newBytes.decodeToString())
                }
            }
        }
    }

    suspend fun updateCommands() {
        val updateAction = guild?.updateCommands() ?: context.jda.updateCommands()
        val commands = updateAction
            .addCommands(allCommandData)
            .await()

        saveCommandData(guild)
        printPushedCommandData(commands, guild)
    }

    private fun computeCommands() = ApplicationCommandDataMap().also { map ->
        computeSlashCommands(guildApplicationCommands, map)
        computeContextCommands(guildApplicationCommands, map, UserCommandInfo::class.java, Command.Type.USER)
        computeContextCommands(guildApplicationCommands, map, MessageCommandInfo::class.java, Command.Type.MESSAGE)
    }

    private fun computeSlashCommands(
        guildApplicationCommands: List<ApplicationCommandInfo>,
        map: ApplicationCommandDataMap
    ) {
        guildApplicationCommands
            .filterIsInstance<SlashCommandInfo>()
            .forEach { info: SlashCommandInfo ->
                val commandPath = info.path
                val description = info.description
                try {
                    val methodOptions = info.getMethodOptions(context, guild)
                    when (commandPath.nameCount) {
                        1 -> {
                            //Standard command
                            val rightCommand = Commands.slash(commandPath.name, description)
                            map.put(Command.Type.SLASH, commandPath, rightCommand)
                            rightCommand.addOptions(methodOptions)
                            configureTopLevel(info, rightCommand)
                        }
                        2 -> {
                            val commandData = map.computeIfAbsent(Command.Type.SLASH, commandPath) {
                                val tmpData = Commands.slash(commandPath.name, "No description (base name)")
                                configureTopLevel(info, tmpData)
                                tmpData
                            } as SlashCommandData

                            val subname =
                                commandPath.subname ?: throwInternal("Command path subname should have not been null")
                            val subcommandData = SubcommandData(subname, description)
                            subcommandData.addOptions(methodOptions)
                            commandData.addSubcommands(subcommandData)
                        }
                        3 -> {
                            val groupData = getSubcommandGroup(map, Command.Type.SLASH, commandPath) {
                                val commandData = Commands.slash(commandPath.name, "No description (base name)")
                                configureTopLevel(info, commandData)
                                commandData
                            }

                            val subname =
                                commandPath.subname ?: throwInternal("Command path subname should have not been null")
                            val subcommandData = SubcommandData(subname, description)
                            subcommandData.addOptions(methodOptions)
                            groupData.addSubcommands(subcommandData)
                        }
                        else -> {
                            throwInternal("A slash command with more than 4 path components got registered")
                        }
                    }
                } catch (e: Exception) {
                    rethrowUser(info.method, "An exception occurred while processing command '$commandPath'", e)
                }
            }
    }

    private fun <T : ApplicationCommandInfo> computeContextCommands(
        guildApplicationCommands: List<ApplicationCommandInfo>,
        map: ApplicationCommandDataMap,
        targetClazz: Class<T>,
        type: Command.Type
    ) {
        guildApplicationCommands
            .filterIsInstance(targetClazz)
            .forEach { info: T ->
                val commandPath = info.path
                try {
                    if (commandPath.nameCount == 1) {
                        //Standard command
                        val rightCommand = Commands.context(type, commandPath.name)
                        map.put(type, commandPath, rightCommand)
                        configureTopLevel(info, rightCommand)
                    } else {
                        throw IllegalStateException("A " + type.name + " command with more than 1 path component got registered")
                    }
                } catch (e: Exception) {
                    rethrowUser(
                        info.method,
                        "An exception occurred while processing a ${type.name} command $commandPath",
                        e
                    )
                }
            }
    }

    private fun configureTopLevel(info: ApplicationCommandInfo, rightCommand: CommandData) {
        if (info.isDefaultLocked) {
            rightCommand.defaultPermissions = DefaultMemberPermissions.DISABLED
        } else if (info.userPermissions.isNotEmpty()) {
            rightCommand.defaultPermissions = DefaultMemberPermissions.enabledFor(info.userPermissions)
        }
    }

    private fun printPushedCommandData(commands: List<Command>, guild: Guild?) {
        if (!LOGGER.isTraceEnabled) return

        val commandNumber = commands.size
        val sentCommandNumber = allCommandData.size
        val cacheViewNumber = context.applicationCommandsView.size
        val scope = guild.asScopeString()

        LOGGER.trace("Updated $commandNumber / $sentCommandNumber ($cacheViewNumber) commands for $scope")
    }

    private fun saveCommandData(guild: Guild?) {
        try {
            commandsCachePath.overwriteBytes(ApplicationCommandsCacheKt.getCommandsBytes(allCommandData))
        } catch (e: Exception) {
            LOGGER.error(
                "An exception occurred while temporarily saving {} commands in '{}'",
                guild.asScopeString(),
                commandsCachePath.toAbsolutePath(),
                e
            )
        }
    }

    //I am aware that the type is always Command.Type#SLASH, still use a parameter to mimic how ApplicationCommandMap functions and for future-proof uses
    private fun getSubcommandGroup(
        map: ApplicationCommandDataMap,
        type: Command.Type,
        path: CommandPath,
        baseCommandSupplier: Function<String, CommandData>
    ): SubcommandGroupData {
        requireNotNull(path.group) { "Group component of command path is null at '$path'" }
        val data = map.computeIfAbsent(type, path, baseCommandSupplier) as SlashCommandData
        val parent = path.parent
            ?: throwInternal("A command path with less than 3 components was passed to #getSubcommandGroup")
        return subcommandGroupDataMap.computeIfAbsent(parent.fullPath) {
            SubcommandGroupData(path.group!!, "No description (group)").also {
                data.addSubcommandGroups(it)
            }
        }
    }

    companion object {
        fun ofGlobal(context: BContextImpl, manager: GlobalApplicationCommandManager): ApplicationCommandsUpdaterKt {
            return ApplicationCommandsUpdaterKt(context, null, manager)
        }

        fun ofGuild(context: BContextImpl, guild: Guild, manager: GuildApplicationCommandManager): ApplicationCommandsUpdaterKt {
            return ApplicationCommandsUpdaterKt(context, guild, manager)
        }
    }
}